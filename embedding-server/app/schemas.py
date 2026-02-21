from typing import Optional

from pydantic import BaseModel, Field


class UpsertDocumentBody(BaseModel):

    source: str = "gitlab"
    gitlab_project_id: int = Field(..., alias="gitlabProjectId")
    gitlab_project_path: str = Field(..., alias="gitlabProjectPath")
    repository_url: str = Field(..., alias="repositoryUrl")
    branch_name: str = Field(..., alias="branchName")
    repository_commit_sha: Optional[str] = Field(None, alias="repositoryCommitSha")
    file_path: str = Field(..., alias="filePath")
    content: str = Field(..., alias="content")
    size_bytes: int = Field(..., alias="sizeBytes")
    metadata: dict[str, str] = Field(default_factory=dict, alias="metadata")

    model_config = {"populate_by_name": True}


class SearchHit(BaseModel):

    file_path: str
    gitlab_project_path: str
    repository_url: str
    branch_name: str
    content_preview: str
    score: float
    document_id: str
