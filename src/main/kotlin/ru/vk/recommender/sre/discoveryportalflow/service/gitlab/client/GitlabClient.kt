package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.config.GitlabProperties
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabCommitDraft
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabFile
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabMergeRequest
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabPipeline
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabProject
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.pathString

class GitlabClient(
    private val gitlabProperties: GitlabProperties,
) {

    fun listProjectFiles(
        projectId: Int,
        ref: String = "master",
        pathFilter: ((String) -> Boolean)? = null,
    ): List<GitlabFile> {
        ndaStub("listProjectFiles")
        return emptyList()
    }

    fun getAllFilesContent(
        projectId: Int,
        files: List<GitlabFile>,
        ref: String = "master",
        replacements: Map<String, String> = emptyMap(),
    ): List<GitlabFile> {
        ndaStub("getAllFilesContent")
        return files
    }

    fun getFileOptional(projectId: Int, filePath: Path, ref: String = "master"): GitlabFile {
        ndaStub("getFileOptional")
        return GitlabFile(path = filePath.pathString, content = null, mustExist = false)
    }

    fun getFile(projectId: Int, filePath: String, ref: String = "master"): GitlabFile {
        ndaStub("getFile")
        return GitlabFile(path = filePath, content = "", mustExist = true)
    }

    fun updateFile(
        projectId: Int,
        filePath: String,
        ref: String = "master",
        updateFunc: (String) -> String,
    ): GitlabFile {
        ndaStub("updateFile")
        return GitlabFile(path = filePath, content = updateFunc(""))
    }

    fun updateFile(projectId: Int, filePath: Path, ref: String = "master", updateFunc: (String) -> String): GitlabFile {
        return updateFile(projectId, filePath.pathString, ref, updateFunc)
    }

    fun createProject(
        name: String,
        path: String,
        namespaceId: Int = gitlabProperties.aiNamespaceId ?: 0,
        visibility: String = "internal",
        initializeWithReadme: Boolean = true,
        defaultBranch: String = "master",
    ): GitlabProject {
        ndaStub("createProject")
        return GitlabProject(
            id = 0,
            name = name,
            path = path,
            pathWithNamespace = path,
            webUrl = null,
        )
    }

    fun getProjectByPath(projectPath: String): GitlabProject? {
        ndaStub("getProjectByPath")
        return null
    }

    fun commit(draft: GitlabCommitDraft): JsonNode {
        ndaStub("commit")
        return mapper.createObjectNode()
    }

    fun createMergeRequest(
        projectId: Int,
        branch: String,
        title: String,
        targetBranch: String = "master",
        squash: Boolean = true,
    ): GitlabMergeRequest {
        ndaStub("createMergeRequest")
        return GitlabMergeRequest(iid = 0, webUrl = null)
    }

    fun createWebhook(
        projectId: Int,
        webhookUrl: String,
        webhookToken: String,
        mergeRequestsEvents: Boolean = true,
        pushEvents: Boolean = false,
        enableSslVerification: Boolean = false,
    ): JsonNode {
        ndaStub("createWebhook")
        return mapper.createObjectNode()
    }

    fun hasWebhook(projectId: Int, webhookUrl: String): Boolean {
        ndaStub("hasWebhook")
        return false
    }

    fun getLatestPipelineForMR(projectId: Int, mrIid: Int): GitlabPipeline? {
        ndaStub("getLatestPipelineForMR")
        return null
    }

    fun retryFailedJobsInPipeline(projectId: Int, pipelineId: Int) {
        ndaStub("retryFailedJobsInPipeline")
    }

    fun enableAutoMergeWhenPipelineSucceeds(projectId: Int, mrIid: Int) {
        ndaStub("enableAutoMergeWhenPipelineSucceeds")
    }

    fun approveMergeRequest(projectId: Int, mrIid: Int) {
        ndaStub("approveMergeRequest")
    }

    fun getMergeRequestDescription(projectId: Int, mrIid: Int): String? {
        ndaStub("getMergeRequestDescription")
        return null
    }

    fun isProjectAlreadyExistsError(exception: IOException): Boolean = false

    fun getRepositoryProjectId(): Int = gitlabProperties.repositoryProjectId ?: 0

    private fun ndaStub(operation: String) {
        // NDA code removed: production implementation calls internal GitLab APIs.
    }

    private companion object {
        private val mapper = jacksonObjectMapper()
    }
}
