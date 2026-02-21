import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, Depends, Query
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

from app.config import Settings
from app.schemas import UpsertDocumentBody, SearchHit
from app.store import EmbeddingStore

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

store: EmbeddingStore | None = None
settings = Settings()
security = HTTPBearer(auto_error=False)


def get_store() -> EmbeddingStore:
    if store is None:
        raise RuntimeError("Store not initialized")
    return store


def check_api_key(
    credentials: HTTPAuthorizationCredentials | None = Depends(security),
) -> None:
    if not settings.api_key:
        return
    token = credentials.credentials if credentials else None
    if token != settings.api_key:
        raise HTTPException(status_code=401, detail="Invalid or missing API key")


@asynccontextmanager
async def lifespan(app: FastAPI):
    global store
    store = EmbeddingStore(
        persist_directory=settings.persist_directory,
        embedding_model=settings.embedding_model,
        collection_name=settings.collection_name,
    )
    logger.info("Embedding store initialized at %s", settings.persist_directory)
    yield
    store = None


app = FastAPI(title="Hi-Wiki Embedding Server", lifespan=lifespan)


@app.get("/health")
def health():
    return {"status": "ok"}


@app.patch("/upsert", status_code=204)
def upsert(body: UpsertDocumentBody, _: None = Depends(check_api_key)):
    get_store().upsert(
        source=body.source,
        gitlab_project_id=body.gitlab_project_id,
        gitlab_project_path=body.gitlab_project_path,
        repository_url=body.repository_url,
        branch_name=body.branch_name,
        repository_commit_sha=body.repository_commit_sha,
        file_path=body.file_path,
        content=body.content,
        size_bytes=body.size_bytes,
        metadata=body.metadata or {},
    )
    return None


@app.get("/search", response_model=list[SearchHit])
def search(
    q: str = Query(..., min_length=1),
    top_k: int = Query(10, ge=1, le=100),
    _: None = Depends(check_api_key),
):
    hits = get_store().search(query=q, top_k=top_k)
    return [SearchHit(**h) for h in hits]
