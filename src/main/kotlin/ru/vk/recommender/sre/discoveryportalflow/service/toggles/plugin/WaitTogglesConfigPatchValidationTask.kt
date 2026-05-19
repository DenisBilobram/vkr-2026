package ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowWaitingTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.client.TogglesClient
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.context.TogglesTenantTaskContext

class WaitTogglesConfigPatchValidationTask(
    private val togglesClient: TogglesClient,
) : FlowWaitingTask<TogglesTenantTaskContext>(TogglesTenantTaskContext::class) {

    override suspend fun check(taskRunContext: TogglesTenantTaskContext): TaskRunResult {
        return try {
            val requestIid = togglesClient.findLastSuccessfulCopyFilesRequestIid(taskRunContext.tenantName)
            if (requestIid == null) {
                runtimeLogger.info(
                    "Toggles config patch validation is waiting for tenant ${taskRunContext.tenantName}",
                )
                TaskRunResult(taskStatus = FlowStatus.WAITING)
            } else {
                taskRunContext.togglesConfigRequestIid = requestIid
                runtimeLogger.info(
                    "Toggles config patch validated for tenant ${taskRunContext.tenantName}: iid=$requestIid",
                )
                TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
            }
        } catch (exception: Exception) {
            runtimeLogger.warn(
                "Toggles config patch validation is not ready for ${taskRunContext.tenantName}: ${exception.message}",
            )
            TaskRunResult(taskStatus = FlowStatus.WAITING)
        }
    }
}
