package com.sleekydz86.ingestion.global.git


import com.sleekydz86.ingestion.domain.model.SourceConfig
import com.sleekydz86.ingestion.domain.port.RepositorySynchronizer
import com.sleekydz86.ingestion.domain.port.SyncResult
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.util.io.DisabledOutputStream
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path

@Component
class JGitRepositorySynchronizer : RepositorySynchronizer {

    override fun syncRepository(
        sourceConfig: SourceConfig,
        projectPath: String,
        targetDirectory: Path,
        mode: String,
    ): SyncResult {
        val repoUrl = if (sourceConfig.baseUrl.endsWith("/")) {
            "${sourceConfig.baseUrl}${projectPath}.git"
        } else {
            "${sourceConfig.baseUrl}/${projectPath}.git"
        }

        if (!Files.exists(targetDirectory)) {
            Files.createDirectories(targetDirectory.parent)
            cloneRepository(repoUrl, targetDirectory, sourceConfig)
            val headSha = resolveHeadCommitSha(targetDirectory)
            return SyncResult(
                repositoryDirectory = targetDirectory,
                isUpdated = true,
                changedFilePaths = null,
                currentCommitSha = headSha,
                branchName = sourceConfig.targetBranch,
            )
        }

        if (mode == "CLONE_OR_PULL") {
            return pullAndDetectChanges(targetDirectory, sourceConfig)
        }

        return SyncResult(
            repositoryDirectory = targetDirectory,
            isUpdated = false,
            changedFilePaths = emptySet(),
            currentCommitSha = resolveHeadCommitSha(targetDirectory),
            branchName = sourceConfig.targetBranch,
        )
    }

    private fun cloneRepository(repoUrl: String, targetDirectory: Path, sourceConfig: SourceConfig) {
        val credentials = createCredentialsProvider(sourceConfig)
        try {
            Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(targetDirectory.toFile())
                .setBranch(sourceConfig.targetBranch)
                .setCredentialsProvider(credentials)
                .call()
                .use { }
        } catch (ex: GitAPIException) {
            throw RuntimeException("Failed to clone repository: ${ex.message}", ex)
        }
    }

    private fun pullAndDetectChanges(targetDirectory: Path, sourceConfig: SourceConfig): SyncResult {
        Git.open(targetDirectory.toFile()).use { git ->
            val repository = git.repository
            checkoutBranch(git, sourceConfig.targetBranch)

            val oldHead = repository.resolve("HEAD")
            val pullResult = git.pull()
                .setCredentialsProvider(createCredentialsProvider(sourceConfig))
                .setRebase(false)
                .setFastForward(org.eclipse.jgit.api.MergeCommand.FastForwardMode.FF_ONLY)
                .call()

            val newHead = repository.resolve("HEAD")

            if (oldHead == null || newHead == null || oldHead == newHead) {
                return SyncResult(
                    repositoryDirectory = targetDirectory,
                    isUpdated = false,
                    changedFilePaths = emptySet(),
                    currentCommitSha = newHead?.name(),
                    branchName = sourceConfig.targetBranch,
                )
            }

            val changedFiles = findChangedFiles(repository, oldHead, newHead)
            return SyncResult(
                repositoryDirectory = targetDirectory,
                isUpdated = pullResult.isSuccessful,
                changedFilePaths = changedFiles,
                currentCommitSha = newHead.name(),
                branchName = sourceConfig.targetBranch,
            )
        }
    }

    private fun findChangedFiles(repository: Repository, oldHead: ObjectId, newHead: ObjectId): Set<String> {
        return RevWalk(repository).use { revWalk ->
            val oldCommit = revWalk.parseCommit(oldHead)
            val newCommit = revWalk.parseCommit(newHead)
            DiffFormatter(DisabledOutputStream.INSTANCE).use { formatter ->
                formatter.setRepository(repository)
                formatter.scan(oldCommit.tree, newCommit.tree)
            }
        }.mapNotNull { diff ->
            if (diff.changeType == DiffEntry.ChangeType.DELETE) {
                null
            } else {
                diff.newPath
            }
        }.filter { it != DiffEntry.DEV_NULL }.toSet()
    }

    private fun resolveHeadCommitSha(targetDirectory: Path): String? {
        return Git.open(targetDirectory.toFile()).use { git ->
            git.repository.resolve("HEAD")?.name()
        }
    }

    private fun checkoutBranch(git: Git, branchName: String) {
        runCatching {
            git.checkout()
                .setName(branchName)
                .setForced(true)
                .call()
        }.recoverCatching {
            git.checkout()
                .setName(branchName)
                .setCreateBranch(true)
                .setStartPoint("origin/$branchName")
                .setForced(true)
                .call()
        }.getOrElse { throw RuntimeException("Failed to checkout branch: $branchName", it) }
    }

    private fun createCredentialsProvider(sourceConfig: SourceConfig): UsernamePasswordCredentialsProvider? {
        return if (sourceConfig.accessToken.isNotBlank()) {
            UsernamePasswordCredentialsProvider("oauth2", sourceConfig.accessToken)
        } else {
            null
        }
    }
}
