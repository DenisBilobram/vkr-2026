package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabFile
import java.nio.file.Path
import kotlin.io.path.pathString

/**
 * @author mikh.nikiforov
 */
class GitlabProjectClient(
    private val gitlabClient: GitlabClient,
    private val projectId: Int,
    private val ref: String = "master",
) {
    private val files = mutableListOf<GitlabFile>()

    fun getFiles(): List<GitlabFile> {
        return files
    }

    fun getFileOptional(filePath: Path): GitlabFile {
        return withCache(filePath.pathString) {
            gitlabClient.getFileOptional(projectId, filePath, ref)
        }
    }

    fun updateFile(
        filePath: String, updateFunc: (String) -> String
    ): GitlabFile {
        return updateWithCache(filePath, updateFunc)
    }

    fun updateFile(filePath: Path, updateFunc: (String) -> String): GitlabFile {
        return updateWithCache(filePath.pathString, updateFunc)
    }

    private fun withCache(key: String, operation: () -> GitlabFile): GitlabFile {
        val cachedFiles = files.filter { it.path == key }.toCollection(mutableListOf())
        if (cachedFiles.none()) {
            val gitlabFile = operation()
            files.add(gitlabFile)
            return gitlabFile
        }
        if (cachedFiles.size > 1) {
            throw RuntimeException("More then 1 file with path $key")
        }
        return cachedFiles[0]
    }

    private fun updateWithCache(filePath: String, updateFunc: (String) -> String): GitlabFile {
        val cachedFiles = files.filter { it.path == filePath }.toCollection(mutableListOf())
        if (cachedFiles.none()) {
            val gitlabFile = gitlabClient.updateFile(projectId, filePath, ref, updateFunc = updateFunc)
            files.add(gitlabFile)
            return gitlabFile
        }
        if (cachedFiles.size > 1) {
            throw RuntimeException("More then 1 file with path $filePath")
        }

        // Apply the update function to the cached file
        val cachedFile = cachedFiles[0]
        cachedFile.content = updateFunc(cachedFile.content!!)
        return cachedFile
    }
}
