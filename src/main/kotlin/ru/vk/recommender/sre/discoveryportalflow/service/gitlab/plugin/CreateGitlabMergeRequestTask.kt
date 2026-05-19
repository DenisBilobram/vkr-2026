package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabMergeRequestTaskContext
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.utils.toGitlabReference

class CreateGitlabMergeRequestTask(
    private val gitlabClient: GitlabClient,
) : FlowTask<GitlabMergeRequestTaskContext>(GitlabMergeRequestTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: GitlabMergeRequestTaskContext): TaskRunResult {
        val draft = taskRunContext.requireDraft()

        val mergeRequest = gitlabClient.createMergeRequest(
            projectId = draft.projectId,
            branch = draft.sourceBranch,
            title = draft.title,
            targetBranch = draft.targetBranch,
            squash = draft.squash,
        )
        runtimeLogger.info(
            "Created GitLab merge request: ${draft.toGitlabReference(iid = mergeRequest.iid)}, " +
                "url=${mergeRequest.webUrl ?: "n/a"}",
        )
        taskRunContext.registerCreatedMergeRequest(mergeRequest)
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
