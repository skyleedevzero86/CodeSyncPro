package com.sleekydz86.ingestion.global.gitlab


import com.sleekydz86.ingestion.domain.model.SourceConfig
import com.sleekydz86.ingestion.domain.port.ProjectCatalog
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.Pager
import org.gitlab4j.api.models.GroupProjectsFilter
import org.gitlab4j.api.models.Project
import org.gitlab4j.api.models.ProjectFilter
import org.springframework.stereotype.Component
import java.util.logging.Logger

@Component
class GitlabProjectCatalog : ProjectCatalog {

    private val logger = Logger.getLogger(GitlabProjectCatalog::class.java.name)

    override fun listProjects(sourceConfig: SourceConfig): List<Project> {
        val gitlabApi = GitLabApi(sourceConfig.baseUrl, sourceConfig.accessToken)

        return when {
            sourceConfig.projectIds.isNotEmpty() -> {
                sourceConfig.projectIds.map { projectId ->
                    val projectData = gitlabApi.projectApi.getProject(projectId)
                    toProject(projectData, gitlabApi, sourceConfig.targetBranch)
                }
            }
            sourceConfig.groupIds.isNotEmpty() -> {
                sourceConfig.groupIds.flatMap { groupId ->
                    val filter = GroupProjectsFilter()
                        .withArchived(sourceConfig.shouldIncludeArchived)
                        .withIncludeSubGroups(sourceConfig.shouldIncludeSubgroups)

                    val pager = gitlabApi.groupApi.getProjects(groupId, filter, sourceConfig.pageSize)
                    loadAllPages(pager).map { projectData ->
                        toProject(projectData, gitlabApi, sourceConfig.targetBranch)
                    }
                }
            }
            else -> {
                val filter = ProjectFilter()
                    .withArchived(sourceConfig.shouldIncludeArchived)
                    .withMembership(sourceConfig.shouldUseMembershipOnly)

                val pager = gitlabApi.projectApi.getProjects(filter, sourceConfig.pageSize)
                loadAllPages(pager).map { projectData ->
                    toProject(projectData, gitlabApi, sourceConfig.targetBranch)
                }
            }
        }
    }

    private fun toProject(
        projectData: Project,
        gitlabApi: GitLabApi,
        targetBranch: String,
    ): Project {
        val headCommit = resolveHeadCommit(projectData, gitlabApi, targetBranch)

        return Project(
            id = projectData.id,
            name = projectData.name,
            pathWithNamespace = projectData.pathWithNamespace,
            httpUrlToRepo = projectData.httpUrlToRepo,
            defaultBranch = projectData.defaultBranch,
            webUrl = projectData.webUrl,
            lastActivityAt = projectData.lastActivityAt?.toInstant(),
            repositoryHeadCommitSha = headCommit?.id,
            repositoryHeadCommittedAt = headCommit?.committedDate?.toInstant(),
        )
    }

    private fun resolveHeadCommit(
        projectData: Project,
        gitlabApi: GitLabApi,
        branchName: String,
    ): org.gitlab4j.api.models.Commit? {
        return runCatching {
            gitlabApi.repositoryApi.getBranch(projectData.id, branchName).commit
        }.onFailure { error ->
            logger.warning(
                "브랜치 커밋 조회 실패: ${projectData.pathWithNamespace} branch=$branchName (${error.message})"
            )
        }.getOrNull()
    }

    private fun <T> loadAllPages(pager: Pager<T>): List<T> = pager.all()
}
