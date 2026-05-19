package ru.vk.recommender.sre.discoveryportalflow.service.yt.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowContextTransformTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.BootstrapRecomContext
import ru.vk.recommender.sre.discoveryportalflow.service.yt.context.YtMockTablesTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.yt.context.YtRuntimeCredentials

class BootstrapYtMockTablesContextTask :
    FlowContextTransformTask<BootstrapRecomContext>(BootstrapRecomContext::class) {

    override suspend fun executeCasted(taskRunContext: BootstrapRecomContext): TaskRunResult {
        runtimeLogger.info(
            "Bootstrapped YT mock tables context for ${taskRunContext.recommender.recommenderName}: " +
                "ytCluster=${taskRunContext.ytCluster.name}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    override fun transformContext(taskRunContext: BootstrapRecomContext): YtMockTablesTaskContext {
        val ytOffline = taskRunContext.oneSecret.ytOffline
        return YtMockTablesTaskContext(
            projectName = taskRunContext.recommender.projectName ?: taskRunContext.recommender.recommenderName,
            recommenderName = taskRunContext.recommender.recommenderName,
            yt = YtRuntimeCredentials(
                users = ytOffline.user,
                tokens = ytOffline.token,
            ),
            ytCluster = taskRunContext.ytCluster,
        )
    }
}
