package com.sleekydz86.ingestion.domain.model

import java.time.Instant

data class ProjectState(
    val projectId: Long,
    val projectPath: String,
    val repositoryUrl: String,
    val versionInstant: Instant,
    val updatedAt: Instant = Instant.now(),
)
