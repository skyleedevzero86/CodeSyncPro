package com.sleekydz86.ingestion.domain.port

import com.sleekydz86.ingestion.domain.model.Project
import com.sleekydz86.ingestion.domain.model.SourceConfig

interface ProjectCatalog {
    fun listProjects(sourceConfig: SourceConfig): List<Project>
}
