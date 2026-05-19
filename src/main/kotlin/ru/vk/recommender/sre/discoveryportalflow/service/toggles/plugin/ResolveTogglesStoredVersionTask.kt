package ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.client.TogglesClient
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.context.TogglesTenantTaskContext

class ResolveTogglesStoredVersionTask(
    private val togglesClient: TogglesClient,
) : FlowTask<TogglesTenantTaskContext>(TogglesTenantTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: TogglesTenantTaskContext): TaskRunResult {
        taskRunContext.togglesStoredVersion = togglesClient.getLastStoredVersion(taskRunContext.tenantName)
        runtimeLogger.info(
            "Resolved stored version for Toggles tenant ${taskRunContext.tenantName}: ${taskRunContext.togglesStoredVersion}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
