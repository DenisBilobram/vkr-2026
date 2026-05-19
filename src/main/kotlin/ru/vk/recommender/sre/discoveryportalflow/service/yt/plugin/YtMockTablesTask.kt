package ru.vk.recommender.sre.discoveryportalflow.service.yt.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.parseNames
import ru.vk.recommender.sre.discoveryportalflow.service.yt.context.YtMockTablesTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.yt.service.YtMockTablesService

class YtMockTablesTask(
    private val ytMockTablesService: YtMockTablesService,
) : FlowTask<YtMockTablesTaskContext>(YtMockTablesTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: YtMockTablesTaskContext): TaskRunResult {
        val result = ytMockTablesService.createMockTables(taskRunContext)

        runtimeLogger.info(
            "Created YT mock tables for ${parseNames(taskRunContext.projectName).folderName}: ${result.tablePaths.joinToString()}",
        )

        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
