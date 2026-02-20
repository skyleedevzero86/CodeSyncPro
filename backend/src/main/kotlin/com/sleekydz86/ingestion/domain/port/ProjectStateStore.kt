package com.sleekydz86.ingestion.domain.port

import com.sleekydz86.ingestion.domain.model.ProjectState

interface ProjectStateStore {
    fun load(): Map<Long, ProjectState>
    fun save(states: Map<Long, ProjectState>)
    fun findByProjectId(projectId: Long): ProjectState?
}
