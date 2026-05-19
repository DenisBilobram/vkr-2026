package ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.client.TogglesClient
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.context.TogglesTenantTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.service.TogglesGoldenSourceService

class CreateTogglesConfigPatchTask(
    private val togglesClient: TogglesClient,
    private val togglesGoldenSourceService: TogglesGoldenSourceService,
) : FlowTask<TogglesTenantTaskContext>(TogglesTenantTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: TogglesTenantTaskContext): TaskRunResult {
        val renderedToggleFlags = togglesGoldenSourceService.loadRenderedToggleFlagFiles(
            sourceProjectId = taskRunContext.goldenSourceProjectId,
            replacements = taskRunContext.replacements,
        )
        taskRunContext.togglesConfigRequestIid = null
        togglesClient.updateConfig(taskRunContext.tenantName, renderedToggleFlags)
        runtimeLogger.info("Created config patch for Toggles tenant ${taskRunContext.tenantName}")
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
