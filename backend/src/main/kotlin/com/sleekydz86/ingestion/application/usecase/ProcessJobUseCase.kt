package com.sleekydz86.ingestion.application.usecase


import com.sleekydz86.ingestion.domain.model.IngestMode
import com.sleekydz86.ingestion.domain.model.ErrorCode
import com.sleekydz86.ingestion.domain.model.ItemError
import com.sleekydz86.ingestion.domain.model.ItemId
import com.sleekydz86.ingestion.domain.model.ItemStatus
import com.sleekydz86.ingestion.domain.model.Job
import com.sleekydz86.ingestion.domain.model.JobId
import com.sleekydz86.ingestion.domain.model.JobItem
import com.sleekydz86.ingestion.domain.model.JobStatus
import com.sleekydz86.ingestion.domain.model.Project
import com.sleekydz86.ingestion.domain.model.ProjectState
import com.sleekydz86.ingestion.domain.port.EmbeddingClient
import com.sleekydz86.ingestion.domain.port.FileScanner
import com.sleekydz86.ingestion.domain.port.JobRepository
import com.sleekydz86.ingestion.domain.port.ProjectCatalog
import com.sleekydz86.ingestion.domain.port.ProjectStateStore
import com.sleekydz86.ingestion.domain.port.RepositorySynchronizer
import com.sleekydz86.ingestion.domain.port.SyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.UUID
import java.util.logging.Logger

@Component
class ProcessJobUseCase(
    private val jobRepository: JobRepository,
    private val projectCatalog: ProjectCatalog,
    private val projectStateStore: ProjectStateStore,
    private val repositorySynchronizer: RepositorySynchronizer,
    private val fileScanner: FileScanner,
    private val embeddingClient: EmbeddingClient,
    private val tempDirectory: Path = Path.of(System.getProperty("java.io.tmpdir"), "ingestion"),
) {
    private val logger: Logger = Logger.getLogger(ProcessJobUseCase::class.java.name)

    suspend fun execute(jobId: JobId) {
        val job = jobRepository.findById(jobId)
            ?: throw JobNotFoundException("Job not found: ${jobId.value}")

        val startedAt = Instant.now()
        val updatedJob = job.markAsProcessing(startedAt)
        jobRepository.update(updatedJob)

        try {
            processJob(updatedJob)
        } catch (e: Exception) {
            logger.severe("Job processing failed: ${e.message}")
            jobRepository.update(updatedJob.markAsFailed(Instant.now(), e.message ?: "Unknown error"))
            throw e
        }
    }

    private suspend fun processJob(job: Job) = coroutineScope {
        val allProjects = withContext(Dispatchers.IO) { projectCatalog.listProjects(job.sourceConfig) }
        logger.info("Found ${allProjects.size} projects")

        val projectsToProcess = when (job.options.mode) {
            IngestMode.INCREMENTAL -> withContext(Dispatchers.IO) { projectStateStore.load() }
                .let { currentStates -> filterChangedProjects(allProjects, currentStates) }
            IngestMode.FULL -> allProjects
        }
        logger.info("Processing ${projectsToProcess.size} projects (mode: ${job.options.mode})")

        if (projectsToProcess.isEmpty()) {
            jobRepository.update(
                job.copy(
                    status = JobStatus.SUCCESS,
                    completedAt = Instant.now(),
                    progress = job.progress.copy(
                        totalProjects = allProjects.size,
                        processedProjects = allProjects.size,
                    ),
                )
            )
            return@coroutineScope
        }

        val semaphore = Semaphore(job.options.concurrency.projects)
        val projectResults = projectsToProcess.map { project ->
            async { semaphore.withPermit { processProject(job, project) } }
        }.awaitAll()

        val updatedStates = projectResults
            .filter { (project, items) -> items.any { it.status == ItemStatus.SUCCESS } && project.versionInstant() != null }
            .mapNotNull { (project, _) ->
                project.versionInstant()?.let { version ->
                    project.id to ProjectState(
                        projectId = project.id,
                        projectPath = project.pathWithNamespace,
                        repositoryUrl = project.httpUrlToRepo,
                        versionInstant = version,
                    )
                }
            }
            .toMap()

        if (updatedStates.isNotEmpty()) {
            withContext(Dispatchers.IO) { projectStateStore.save(updatedStates) }
        }

        val totalFiles = projectResults.sumOf { (_, items) -> items.count { it.status == ItemStatus.SUCCESS } }
        val failedFiles = projectResults.sumOf { (_, items) -> items.count { it.status == ItemStatus.FAILED } }
        val totalItemCount = projectResults.sumOf { (_, items) -> items.size }

        val finalJob = job.copy(
            progress = job.progress.copy(
                totalProjects = allProjects.size,
                processedProjects = projectsToProcess.size,
                totalFiles = totalItemCount,
                processedFiles = totalFiles,
                failedFiles = failedFiles,
            ),
        )
        val finalStatus = computeFinalStatus(finalJob, totalFiles, failedFiles, Instant.now())
        jobRepository.update(finalStatus)
    }

    private fun computeFinalStatus(job: Job, totalFiles: Int, failedFiles: Int, completedAt: Instant): Job =
        when {
            failedFiles == 0 -> job.markAsSuccess(completedAt)
            totalFiles > 0 -> job.markAsPartialSuccess(completedAt)
            else -> job.markAsFailed(completedAt, "All files failed")
        }

    private fun filterChangedProjects(
        projects: List<Project>,
        currentStates: Map<Long, ProjectState>,
    ): List<Project> = projects.filter { project ->
        val currentVersion = project.versionInstant()
        when {
            currentVersion == null -> {
                logger.info("Project ${project.pathWithNamespace} has no version, will process")
                true
            }
            currentStates[project.id] == null -> {
                logger.info("Project ${project.pathWithNamespace} not in state, will process")
                true
            }
            else -> {
                val lastState = currentStates[project.id]!!
                val shouldProcess = currentVersion.isAfter(lastState.versionInstant)
                if (shouldProcess) {
                    logger.info(
                        "Project ${project.pathWithNamespace} changed: " +
                            "current=$currentVersion last=${lastState.versionInstant}"
                    )
                }
                shouldProcess
            }
        }
    }

    private suspend fun processProject(
        job: Job,
        project: Project,
    ): Pair<Project, List<JobItem>> = coroutineScope {
        logger.info("Processing project: ${project.pathWithNamespace}")

        val syncResult = withContext(Dispatchers.IO) {
            val repoDir = tempDirectory.resolve("${job.id.value}-${project.id}-${UUID.randomUUID()}")
            Files.createDirectories(repoDir)

            repositorySynchronizer.syncRepository(
                sourceConfig = job.sourceConfig,
                projectPath = project.pathWithNamespace,
                targetDirectory = repoDir,
                mode = job.options.mode.name,
            )
        }

        val changedFilePaths = when {
            job.options.mode == IngestMode.INCREMENTAL &&
                syncResult.changedFilePaths != null &&
                syncResult.changedFilePaths!!.isEmpty() -> {
                logger.info("No changed files in ${project.pathWithNamespace}")
                return@coroutineScope project to emptyList<JobItem>()
            }
            job.options.mode == IngestMode.INCREMENTAL -> syncResult.changedFilePaths
            else -> null
        }

        val scannedFiles = withContext(Dispatchers.IO) {
            fileScanner.scanFiles(
                repositoryDirectory = syncResult.repositoryDirectory,
                fileFilters = job.options.fileFilters,
                changedFilePaths = changedFilePaths,
            )
        }

        if (scannedFiles.isEmpty()) {
            logger.info("No eligible files in ${project.pathWithNamespace}")
            if (job.options.cleanupAfterIngest) {
                cleanupDirectory(syncResult.repositoryDirectory)
            }
            return@coroutineScope project to emptyList<JobItem>()
        }

        logger.info("Scanned ${scannedFiles.size} files in ${project.pathWithNamespace}")

        val items = scannedFiles.map { file ->
            JobItem(
                id = ItemId(UUID.randomUUID().toString()),
                jobId = job.id,
                projectPath = project.pathWithNamespace,
                filePath = file.filePath,
                status = ItemStatus.PENDING,
            )
        }

        val concurrencyLimit = job.options.concurrency.files
        val semaphore = Semaphore(concurrencyLimit)

        val processedItems = items.map { item ->
            async {
                semaphore.withPermit {
                    processItem(job, item, project, syncResult)
                }
            }
        }.awaitAll()

        if (job.options.cleanupAfterIngest) {
            cleanupDirectory(syncResult.repositoryDirectory)
        }

        project to processedItems
    }

    private suspend fun processItem(
        job: Job,
        item: JobItem,
        project: Project,
        syncResult: SyncResult,
    ): JobItem {
        val updatedItem = item.copy(status = ItemStatus.PROCESSING)

        return try {
            fileScanner.scanFiles(
                repositoryDirectory = syncResult.repositoryDirectory,
                fileFilters = job.options.fileFilters,
                changedFilePaths = setOf(item.filePath),
            ).firstOrNull()
                ?.let { file ->
                    embeddingClient.upsertDocument(
                        sourceConfig = job.sourceConfig,
                        projectPath = project.pathWithNamespace,
                        filePath = item.filePath,
                        content = file.content,
                        commitSha = syncResult.currentCommitSha,
                        branchName = syncResult.branchName,
                    )
                    updatedItem.markAsSuccess(Instant.now())
                }
                ?: updatedItem.markAsSkipped("File not found")
        } catch (e: Exception) {
            logger.warning("Failed to process item ${item.filePath}: ${e.message}")
            updatedItem.markAsFailed(
                ItemError(
                    code = when {
                        e.message?.contains("timeout", ignoreCase = true) == true -> ErrorCode.TIMEOUT
                        e.message?.contains("network", ignoreCase = true) == true -> ErrorCode.NETWORK_ERROR
                        else -> ErrorCode.UNKNOWN_ERROR
                    },
                    message = e.message ?: "Unknown error",
                    retryable = true,
                )
            )
        }
    }

    private suspend fun cleanupDirectory(directory: Path) =
        withContext(Dispatchers.IO) {
            runCatching {
                Files.walk(directory).sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
            }.onFailure { e -> logger.warning("Failed to cleanup directory $directory: ${e.message}") }
        }
}
