package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.service.OneSecretQueueTargetResolver
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.BootstrapRecomContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.plugin.RecommenderRuntimeBootstrapTask
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecomRuntimeContextFactory

class BootstrapOneSecretContextTask(
    private val oneSecretQueueTargetResolver: OneSecretQueueTargetResolver,
    recomRuntimeContextFactory: RecomRuntimeContextFactory,
) : RecommenderRuntimeBootstrapTask(recomRuntimeContextFactory) {

    override suspend fun executeCasted(taskRunContext: BootstrapRecomContext): TaskRunResult {
        runtimeLogger.info("Bootstrapped oneSecret context for ${taskRunContext.recommender.recommenderName}")
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    override fun transformContext(taskRunContext: BootstrapRecomContext): OneSecretTaskContext {
        val runtimeContext = buildRuntimeContext(taskRunContext)
        val queueTargetPlan = oneSecretQueueTargetResolver.resolveQueueTargetPlan(
            recommenderRuntime = runtimeContext.recommender,
            serviceRuntimes = runtimeContext.services,
            dcSettings = runtimeContext.dcSettings,
        )

        return OneSecretTaskContext(
            workspaceRoot = runtimeContext.workspaceRoot,
            recommenderName = runtimeContext.recommenderName,
            productId = runtimeContext.recommender.productId,
            projectName = runtimeContext.projectName,
            projectProductId = runtimeContext.recommender.projectProductId,
            apptracerToken = taskRunContext.apptracerToken,
            ytOffline = taskRunContext.oneSecret.ytOffline,
            redis = taskRunContext.oneSecret.redis,
            verticalQueueTargets = queueTargetPlan.verticalTargets,
            projectQueueTargets = queueTargetPlan.projectTargets,
            serviceTargets = queueTargetPlan.serviceTargets,
        )
    }
}
