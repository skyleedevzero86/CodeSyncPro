package com.sleekydz86.ingestion.domain.model

import java.time.Instant

data class Project(
    val id: Long,
    val name: String,
    val pathWithNamespace: String,
    val httpUrlToRepo: String,
    val defaultBranch: String?,
    val webUrl: String?,
    val lastActivityAt: Instant?,
    val repositoryHeadCommitSha: String?,
    val repositoryHeadCommittedAt: Instant?,
) {
    fun versionInstant(): Instant? = repositoryHeadCommittedAt ?: lastActivityAt
}
