package ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.client.DrillsClient
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.context.TogglesTenantTaskContext

class CreateDrillsProjectTask(
    private val drillsClient: DrillsClient,
) : FlowTask<TogglesTenantTaskContext>(TogglesTenantTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: TogglesTenantTaskContext): TaskRunResult {
        drillsClient.createProject(taskRunContext.tenantName)
        runtimeLogger.info("Created Drills project for Toggles tenant ${taskRunContext.tenantName}")
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
