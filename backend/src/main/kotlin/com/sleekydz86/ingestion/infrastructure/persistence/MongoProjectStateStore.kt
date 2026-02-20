package com.sleekydz86.ingestion.infrastructure.persistence


import com.sleekydz86.ingestion.domain.model.ProjectState
import com.sleekydz86.ingestion.domain.port.ProjectStateStore
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class MongoProjectStateStore(
    private val mongoTemplate: MongoTemplate,
) : ProjectStateStore {

    override fun load(): Map<Long, ProjectState> {
        val documents = mongoTemplate.findAll(ProjectStateDocument::class.java, "project_states")
        return documents.map { doc ->
            doc.projectId to ProjectState(
                projectId = doc.projectId,
                projectPath = doc.projectPath,
                repositoryUrl = doc.repositoryUrl,
                versionInstant = Instant.parse(doc.versionInstant),
                updatedAt = Instant.parse(doc.updatedAt),
            )
        }.toMap()
    }

    override fun save(states: Map<Long, ProjectState>) {
        states.forEach { (_, state) ->
            val document = ProjectStateDocument(
                projectId = state.projectId,
                projectPath = state.projectPath,
                repositoryUrl = state.repositoryUrl,
                versionInstant = state.versionInstant.toString(),
                updatedAt = state.updatedAt.toString(),
            )
            mongoTemplate.save(document, "project_states")
        }
    }

    override fun findByProjectId(projectId: Long): ProjectState? {
        val query = Query(Criteria.where("projectId").`is`(projectId))
        val document = mongoTemplate.findOne(query, ProjectStateDocument::class.java, "project_states")
        return document?.let {
            ProjectState(
                projectId = it.projectId,
                projectPath = it.projectPath,
                repositoryUrl = it.repositoryUrl,
                versionInstant = Instant.parse(it.versionInstant),
                updatedAt = Instant.parse(it.updatedAt),
            )
        }
    }
}

data class ProjectStateDocument(
    val projectId: Long,
    val projectPath: String,
    val repositoryUrl: String,
    val versionInstant: String,
    val updatedAt: String,
)
