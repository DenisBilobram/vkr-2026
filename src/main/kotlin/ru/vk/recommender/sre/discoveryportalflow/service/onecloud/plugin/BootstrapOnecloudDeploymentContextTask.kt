package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder.OnecloudDeploymentContextBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context.OnecloudDeploymentTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.BootstrapRecomContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.plugin.RecommenderRuntimeBootstrapTask
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecomRuntimeContextFactory

class BootstrapOnecloudDeploymentContextTask(
    private val onecloudDeploymentContextBuilder: OnecloudDeploymentContextBuilder,
    recomRuntimeContextFactory: RecomRuntimeContextFactory,
) : RecommenderRuntimeBootstrapTask(recomRuntimeContextFactory) {

    override suspend fun executeCasted(taskRunContext: BootstrapRecomContext): TaskRunResult {
        val onecloudContext = buildOnecloudContext(taskRunContext)
        runtimeLogger.info(
            "Bootstrapped OneCloud deployment context for ${taskRunContext.recommender.recommenderName}: " +
                "queues=${onecloudContext.onecloudSubmitQueuesTaskContext.queueSubmissions.size}, " +
                "storages=${onecloudContext.onecloudSubmitStoragesTaskContext.storageSubmissions.size}, " +
                "services=${onecloudContext.onecloudSubmitServicesTaskContext.serviceSubmissions.size}, " +
                "waitTargets=${onecloudContext.onecloudWaitServicesRunningTaskContext.serviceTargets.size}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    override fun transformContext(taskRunContext: BootstrapRecomContext): OnecloudDeploymentTaskContext {
        return buildOnecloudContext(taskRunContext)
    }

    private fun buildOnecloudContext(
        taskRunContext: BootstrapRecomContext,
    ): OnecloudDeploymentTaskContext {
        return onecloudDeploymentContextBuilder.build(buildRuntimeContext(taskRunContext))
    }
}
