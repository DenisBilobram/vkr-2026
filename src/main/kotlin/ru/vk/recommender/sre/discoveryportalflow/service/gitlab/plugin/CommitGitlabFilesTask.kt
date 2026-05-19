package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabCommitTaskContext

class CommitGitlabFilesTask(
    private val gitlabClient: GitlabClient,
) : FlowTask<GitlabCommitTaskContext>(GitlabCommitTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: GitlabCommitTaskContext): TaskRunResult {
        val draft = taskRunContext.requireDraft()
        gitlabClient.commit(draft)
        runtimeLogger.info(
            "Committed ${draft.files.size} file(s) to GitLab project ${draft.projectId} on branch ${draft.branch}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
