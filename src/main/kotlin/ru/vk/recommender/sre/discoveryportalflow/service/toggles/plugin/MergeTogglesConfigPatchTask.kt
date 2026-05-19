package ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.client.TogglesClient
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.context.TogglesTenantTaskContext

class MergeTogglesConfigPatchTask(
    private val togglesClient: TogglesClient,
) : FlowTask<TogglesTenantTaskContext>(TogglesTenantTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: TogglesTenantTaskContext): TaskRunResult {
        val requestIid = requireNotNull(taskRunContext.togglesConfigRequestIid) {
            "Toggles config request iid is required for tenant ${taskRunContext.tenantName}"
        }
        togglesClient.merge(taskRunContext.tenantName, requestIid)
        runtimeLogger.info("Merged config patch for Toggles tenant ${taskRunContext.tenantName}")
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
