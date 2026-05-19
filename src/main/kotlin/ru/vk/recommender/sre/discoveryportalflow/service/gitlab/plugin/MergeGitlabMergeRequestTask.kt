package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabMergeRequestTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.utils.toGitlabReference

class MergeGitlabMergeRequestTask(
    private val gitlabClient: GitlabClient,
) : FlowTask<GitlabMergeRequestTaskContext>(GitlabMergeRequestTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: GitlabMergeRequestTaskContext): TaskRunResult {
        val draft = taskRunContext.requireDraft()
        val mrIid = taskRunContext.requireMergeRequestIid()

        gitlabClient.enableAutoMergeWhenPipelineSucceeds(draft.projectId, mrIid)
        runtimeLogger.info(
            "Enabled GitLab auto-merge: ${draft.toGitlabReference(iid = mrIid)}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
