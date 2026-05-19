package ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.client.TogglesClient
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.context.TogglesTenantTaskContext

class CreateTogglesReleaseTask(
    private val togglesClient: TogglesClient,
) : FlowTask<TogglesTenantTaskContext>(TogglesTenantTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: TogglesTenantTaskContext): TaskRunResult {
        taskRunContext.togglesStoredVersion = null
        togglesClient.createRelease(taskRunContext.tenantName)
        runtimeLogger.info("Created release for Toggles tenant ${taskRunContext.tenantName}")
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
