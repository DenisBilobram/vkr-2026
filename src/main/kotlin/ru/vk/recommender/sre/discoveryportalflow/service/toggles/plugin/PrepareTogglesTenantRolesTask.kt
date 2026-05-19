package ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.context.TogglesTenantTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.service.TogglesTenantRolesService

class PrepareTogglesTenantRolesTask(
    private val togglesTenantRolesService: TogglesTenantRolesService,
) : FlowTask<TogglesTenantTaskContext>(TogglesTenantTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: TogglesTenantTaskContext): TaskRunResult {
        val rolesDraft = togglesTenantRolesService.buildRolesDraft(taskRunContext.tenantName, taskRunContext.owner)
        taskRunContext.gitlabCommitTaskContext.prepareDraft(rolesDraft.commitDraft)
        taskRunContext.gitlabMergeRequestTaskContext.prepareDraft(rolesDraft.mergeRequestDraft)
        runtimeLogger.info("Prepared tenant roles changes for ${taskRunContext.tenantName}")
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
