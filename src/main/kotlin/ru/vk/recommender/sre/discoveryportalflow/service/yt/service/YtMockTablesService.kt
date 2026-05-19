package ru.vk.recommender.sre.discoveryportalflow.service.yt.service

import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.parseNames
import ru.vk.recommender.sre.discoveryportalflow.service.yt.client.YtRuntimeClientFactory
import ru.vk.recommender.sre.discoveryportalflow.service.yt.context.YtMockTablesTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.yt.model.YtMockTablePathResolver
import ru.vk.recommender.sre.discoveryportalflow.service.yt.model.YtMockTablePaths
import ru.vk.recommender.sre.discoveryportalflow.service.yt.model.YtMockTablesResult

class YtMockTablesService(
    private val ytRuntimeClientFactory: YtRuntimeClientFactory,
) {

    fun createMockTables(context: YtMockTablesTaskContext): YtMockTablesResult {
        validateProjectName(parseNames(context.projectName).folderName)

        val tablePaths = tablePaths(context)
        val runtimeClients = ytRuntimeClientFactory.create(
            credentials = context.yt,
            tmpDir = tablePaths.tmp,
            ytCluster = context.ytCluster
        )
        runtimeClients.use {
            // NDA code removed: production implementation creates mock YT tables and writes sample rows here.
            return YtMockTablesResult(tablePaths = tablePaths.asStrings())
        }
    }

    private fun tablePaths(context: YtMockTablesTaskContext): YtMockTablePaths {
        return YtMockTablePathResolver.resolve(
            rootPath = context.rootPath,
            projectName = context.projectName,
            recommenderName = context.recommenderName,
            embeddingVersion = context.embeddingVersion,
        )
    }

    private fun validateProjectName(projectName: String) {
        require(PROJECT_NAME_PATTERN.matches(projectName)) {
            "Project name must match ${PROJECT_NAME_PATTERN.pattern}: $projectName"
        }
    }

    companion object {
        private val PROJECT_NAME_PATTERN = Regex("[A-Za-z0-9._-]+")
    }
}
