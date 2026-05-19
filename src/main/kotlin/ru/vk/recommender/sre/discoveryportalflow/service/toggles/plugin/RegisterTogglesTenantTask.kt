package ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.client.TogglesClient
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.context.TogglesTenantTaskContext

class RegisterTogglesTenantTask(
    private val togglesClient: TogglesClient,
) : FlowTask<TogglesTenantTaskContext>(TogglesTenantTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: TogglesTenantTaskContext): TaskRunResult {
        val gitlabProjectInfo = taskRunContext.gitlabProjectTaskContext.gitlabProjectInfo
        val projectId = requireNotNull(gitlabProjectInfo.gitlabProjectId) {
            "GitLab project id is required to register toggles tenant ${taskRunContext.tenantName}"
        }
        togglesClient.registerTenant(
            tenantName = taskRunContext.tenantName,
            projectId = projectId,
            dcs = taskRunContext.dcs,
            abcIds = taskRunContext.abcIds,
            repositoryPath = gitlabProjectInfo.gitlabRepositoryPath,
        )
        runtimeLogger.info("Registered Toggles tenant ${taskRunContext.tenantName}")
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
