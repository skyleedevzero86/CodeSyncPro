import hashlib
import logging
import os
from pathlib import Path
from typing import List, Optional

import chromadb
from chromadb.api.types import Documents, Embeddings
from chromadb.config import Settings as ChromaSettings

logger = logging.getLogger(__name__)

COLLECTION_NAME = "gitlab_documents"
MAX_CONTENT_LENGTH = 8000


class _SentenceTransformerEmbeddingFunction:
    """ChromaDB 0.5 compatible embedding function: __call__(self, input) -> Embeddings."""

    def __init__(self, model_name: str):
        self._model_name = model_name
        self._model = None

    def _get_model(self):
        if self._model is None:
            from sentence_transformers import SentenceTransformer
            self._model = SentenceTransformer(self._model_name)
        return self._model

    def __call__(self, input: Documents) -> Embeddings:
        if not input:
            return []
        model = self._get_model()
        embeddings = model.encode(input, show_progress_bar=False)
        return embeddings.tolist()


def _doc_id(repository_url: str, branch_name: str, file_path: str, commit_sha: Optional[str]) -> str:
    raw = f"{repository_url}|{branch_name}|{file_path}|{commit_sha or ''}"
    return hashlib.sha256(raw.encode()).hexdigest()[:32]


def _truncate_for_embedding(content: str, max_chars: int = MAX_CONTENT_LENGTH) -> str:
    if len(content) <= max_chars:
        return content
    return content[:max_chars] + "\n\n[... truncated ...]"


class EmbeddingStore:

    def __init__(
        self,
        persist_directory: str = "./data/chroma",
        embedding_model: str = "sentence-transformers/all-MiniLM-L6-v2",
        collection_name: str = COLLECTION_NAME,
    ):
        self._persist_dir = Path(persist_directory)
        self._persist_dir.mkdir(parents=True, exist_ok=True)
        self._collection_name = collection_name
        self._embedding_model_name = embedding_model
        self._client: Optional[chromadb.PersistentClient] = None
        self._collection = None

    def _get_client(self) -> chromadb.PersistentClient:
        if self._client is None:
            self._client = chromadb.PersistentClient(
                path=str(self._persist_dir),
                settings=ChromaSettings(anonymized_telemetry=False),
            )
        return self._client

    def _get_collection(self):
        if self._collection is not None:
            return self._collection
        client = self._get_client()
        embedding_function = _SentenceTransformerEmbeddingFunction(self._embedding_model_name)
        self._collection = client.get_or_create_collection(
            name=self._collection_name,
            metadata={"description": "GitLab ingested files for RAG"},
            embedding_function=embedding_function,
        )
        return self._collection

    def upsert(
        self,
        *,
        source: str,
        gitlab_project_id: int,
        gitlab_project_path: str,
        repository_url: str,
        branch_name: str,
        repository_commit_sha: Optional[str],
        file_path: str,
        content: str,
        size_bytes: int,
        metadata: dict,
    ) -> None:
        if not content.strip():
            logger.warning("Skipping empty content for path=%s", file_path)
            return
        doc_id = _doc_id(repository_url, branch_name, file_path, repository_commit_sha)
        text_to_embed = _truncate_for_embedding(content)
        coll = self._get_collection()
        meta = {
            "source": source,
            "gitlab_project_id": gitlab_project_id,
            "gitlab_project_path": gitlab_project_path,
            "repository_url": repository_url,
            "branch_name": branch_name,
            "repository_commit_sha": repository_commit_sha or "",
            "file_path": file_path,
            "size_bytes": size_bytes,
        }
        meta.update(metadata)
        coll.upsert(
            ids=[doc_id],
            documents=[text_to_embed],
            metadatas=[meta],
        )
        logger.info("Upserted doc_id=%s file_path=%s", doc_id, file_path)

    def search(self, query: str, top_k: int = 10) -> list[dict]:
        coll = self._get_collection()
        n = min(top_k, 100)
        result = coll.query(query_texts=[query], n_results=n, include=["documents", "metadatas", "distances"])
        if not result["ids"] or not result["ids"][0]:
            return []
        out = []
        ids = result["ids"][0]
        docs = result["documents"][0]
        metas = result["metadatas"][0]
        dists = result["distances"][0]
        for i, doc_id in enumerate(ids):
            dist = dists[i] if dists is not None else 0.0
            score = 1.0 / (1.0 + dist) if dist is not None else 1.0
            meta = metas[i] if metas else {}
            doc_text = docs[i] if docs else ""
            preview = (doc_text[:500] + "...") if len(doc_text) > 500 else doc_text
            out.append({
                "document_id": doc_id,
                "file_path": meta.get("file_path", ""),
                "gitlab_project_path": meta.get("gitlab_project_path", ""),
                "repository_url": meta.get("repository_url", ""),
                "branch_name": meta.get("branch_name", ""),
                "content_preview": preview,
                "score": round(score, 4),
            })
        return out
