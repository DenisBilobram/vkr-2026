package ru.vk.recommender.sre.discoveryportalflow.service.ocs.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowContextTransformTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.ocs.context.OcsTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.BootstrapRecomContext

class BootstrapOcsContextTask : FlowContextTransformTask<BootstrapRecomContext>(BootstrapRecomContext::class) {

    override suspend fun executeCasted(taskRunContext: BootstrapRecomContext): TaskRunResult {
        runtimeLogger.info("Bootstrapped OCS context for ${taskRunContext.recommender.recommenderName}")
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    override fun transformContext(taskRunContext: BootstrapRecomContext): OcsTaskContext {
        return OcsTaskContext(
            recommenderName = taskRunContext.recommender.recommenderName,
            serviceOwner = taskRunContext.recommender.serviceOwner,
            dcSettings = taskRunContext.dcSettings,
        )
    }
}
