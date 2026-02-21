# Embedding Server

GitLab Ingestion 파이프라인에서 전송하는 문서를 받아 **임베딩을 생성하고 벡터 저장소에 저장**하며, **RAG용 의미 검색**을 제공하는 서비스입니다.

## 역할

- **Upsert API (`PATCH /upsert`)**: [gitlab-ingestion](../gitlab-ingestion)이 파일 단위로 보내는 JSON을 수신 → 텍스트 임베딩 생성 → Chroma에 저장
- **Search API (`GET /search?q=...`)**: 저장된 문서에 대한 시맨틱 검색 (RAG 검색용)
- **영속 저장**: Chroma 디렉터리(기본 `./data/chroma`)에 벡터 저장 → 재시작 후에도 유지

## 요구 사항

- Python 3.12+
- (선택) Docker

## 설정 (환경 변수)

| 변수                      | 설명                           | 기본값                                   |
| ------------------------- | ------------------------------ | ---------------------------------------- |
| `EMBED_HOST`              | 바인딩 주소                    | `0.0.0.0`                                |
| `EMBED_PORT`              | 포트                           | `8000`                                   |
| `EMBED_PERSIST_DIRECTORY` | Chroma 저장 경로               | `./data/chroma`                          |
| `EMBED_EMBEDDING_MODEL`   | sentence-transformers 모델명   | `sentence-transformers/all-MiniLM-L6-v2` |
| `EMBED_COLLECTION_NAME`   | Chroma 컬렉션 이름             | `gitlab_documents`                       |
| `EMBED_API_KEY`           | Bearer 인증 키 (비우면 미사용) | (없음)                                   |

Docker Compose로 둘 다 실행할 때는 ingestion 앱이 같은 네트워크에서 `http://embedding-server:8000/upsert`로 연결하면 됩니다.

## 기술 스택

- **FastAPI**: HTTP API
- **Chroma**: 벡터 저장소 (영속)
- **sentence-transformers**: 로컬 임베딩 모델 (all-MiniLM-L6-v2)
