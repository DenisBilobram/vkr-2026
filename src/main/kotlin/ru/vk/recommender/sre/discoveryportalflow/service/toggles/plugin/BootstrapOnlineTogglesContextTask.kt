package ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowContextTransformTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.BootstrapRecomContext
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.context.TogglesTenantTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.service.TogglesTenantContextFactory

class BootstrapOnlineTogglesContextTask(
    private val togglesTenantContextFactory: TogglesTenantContextFactory,
) : FlowContextTransformTask<BootstrapRecomContext>(BootstrapRecomContext::class) {

    override suspend fun executeCasted(taskRunContext: BootstrapRecomContext): TaskRunResult {
        runtimeLogger.info(
            "Bootstrapped online Toggles context for ${taskRunContext.recommender.recommenderName}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    override fun transformContext(taskRunContext: BootstrapRecomContext): TogglesTenantTaskContext {
        return togglesTenantContextFactory.buildOnline(taskRunContext)
    }
}
