package ru.vk.recommender.sre.discoveryportalflow.service.toggles.service

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabFile

class TogglesGoldenSourceService(
    private val gitlabClient: GitlabClient,
) {

    fun loadRenderedToggleFlagFiles(
        sourceProjectId: Int,
        replacements: Map<String, String>,
    ): List<GitlabFile> {
        val files = gitlabClient.listProjectFiles(
            projectId = sourceProjectId,
            pathFilter = ::isToggleFlagFile,
        )
        println("Found toggle flag files in golden source: ${files.size}")

        return gitlabClient.getAllFilesContent(
            projectId = sourceProjectId,
            files = files,
            replacements = replacements,
        ).also {
            println("Downloaded and rendered toggle flag files from golden source")
        }
    }

    private fun isToggleFlagFile(path: String): Boolean {
        return path.startsWith("flags/") && path.endsWith(".flag")
    }
}
