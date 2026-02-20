package com.sleekydz86.ingestion.infrastructure.persistence


import com.sleekydz86.ingestion.domain.model.Job
import com.sleekydz86.ingestion.domain.model.JobId
import com.sleekydz86.ingestion.domain.model.JobStatus
import com.sleekydz86.ingestion.domain.port.JobRepository
import com.sleekydz86.ingestion.infrastructure.persistence.mapper.JobMapper
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class MongoJobRepository(
    private val mongoTemplate: MongoTemplate,
    private val jobMapper: JobMapper,
) : JobRepository {

    override fun save(job: Job): Job {
        val document = jobMapper.toDocument(job)
        mongoTemplate.save(document, "jobs")
        return job
    }

    override fun findById(jobId: JobId): Job? {
        val query = Query(Criteria.where("_id").`is`(jobId.value))
        val document = mongoTemplate.findOne(query, JobDocument::class.java, "jobs")
        return document?.let { jobMapper.toDomain(it) }
    }

    override fun findByStatus(status: JobStatus, limit: Int, offset: Int): List<Job> {
        val query = Query(Criteria.where("status").`is`(status.name))
            .limit(limit)
            .skip(offset)
        val documents = mongoTemplate.find(query, JobDocument::class.java, "jobs")
        return documents.map { jobMapper.toDomain(it) }
    }

    override fun findAll(limit: Int, offset: Int): List<Job> {
        val query = Query()
            .limit(limit)
            .skip(offset)
        val documents = mongoTemplate.find(query, JobDocument::class.java, "jobs")
        return documents.map { jobMapper.toDomain(it) }
    }

    override fun findByCreatedAtBetween(from: Instant, to: Instant, limit: Int, offset: Int): List<Job> {
        val criteria = Criteria.where("createdAt").gte(from).lte(to)
        val query = Query(criteria).with(Sort.by(Sort.Direction.DESC, "createdAt"))
            .limit(limit)
            .skip(offset)
        val documents = mongoTemplate.find(query, JobDocument::class.java, "jobs")
        return documents.map { jobMapper.toDomain(it) }
    }

    override fun update(job: Job): Job {
        val document = jobMapper.toDocument(job)
        mongoTemplate.save(document, "jobs")
        return job
    }

    override fun delete(jobId: JobId) {
        val query = Query(Criteria.where("_id").`is`(jobId.value))
        mongoTemplate.remove(query, "jobs")
    }
}
