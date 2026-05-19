package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabMergeRequestTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.utils.toGitlabReference

class ApproveGitlabMergeRequestTask(
    private val gitlabClient: GitlabClient,
) : FlowTask<GitlabMergeRequestTaskContext>(GitlabMergeRequestTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: GitlabMergeRequestTaskContext): TaskRunResult {
        val draft = taskRunContext.requireDraft()
        val mrIid = taskRunContext.gitlabMergeRequestIid!!

        gitlabClient.approveMergeRequest(
            projectId = draft.projectId,
            mrIid = mrIid
        )
        runtimeLogger.info(
            "Approved GitLab merge request: ${draft.toGitlabReference(iid = mrIid)}, url=${taskRunContext.gitlabMergeRequestUrl!!}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
