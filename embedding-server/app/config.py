from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    host: str = "0.0.0.0"
    port: int = 8000
    persist_directory: str = "./data/chroma"
    embedding_model: str = "sentence-transformers/all-MiniLM-L6-v2"
    collection_name: str = "gitlab_documents"
    api_key: str | None = None

    model_config = {"env_prefix": "EMBED_", "extra": "ignore"}
