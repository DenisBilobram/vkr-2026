package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowWaitingTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabMergeRequestTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.utils.toGitlabReference

class WaitGitlabMergeRequestPipelineTask(
    private val gitlabClient: GitlabClient,
) : FlowWaitingTask<GitlabMergeRequestTaskContext>(GitlabMergeRequestTaskContext::class) {

    override suspend fun check(taskRunContext: GitlabMergeRequestTaskContext): TaskRunResult {
        val draft = taskRunContext.requireDraft()
        val mrIid = taskRunContext.requireMergeRequestIid()

        return try {
            val latestPipeline = gitlabClient.getLatestPipelineForMR(draft.projectId, mrIid)
            when {
                latestPipeline == null -> {
                    runtimeLogger.info(
                        "GitLab pipeline is waiting: ${draft.toGitlabReference(iid = mrIid)}, no pipeline found",
                    )
                    TaskRunResult(taskStatus = FlowStatus.WAITING)
                }

                latestPipeline.status in WAITING_PIPELINE_STATUSES -> {
                    runtimeLogger.info(
                        "GitLab pipeline is waiting: ${draft.toGitlabReference(iid = mrIid)}, pipeline=${latestPipeline.id}, " +
                            "status=${latestPipeline.status}",
                    )
                    TaskRunResult(taskStatus = FlowStatus.WAITING)
                }

                latestPipeline.status in RETRYABLE_PIPELINE_STATUSES -> {
                    runtimeLogger.warn(
                        "GitLab pipeline failed, retrying jobs: ${draft.toGitlabReference(iid = mrIid)}, " +
                            "pipeline=${latestPipeline.id}, status=${latestPipeline.status}",
                    )
                    gitlabClient.retryFailedJobsInPipeline(draft.projectId, latestPipeline.id)
                    TaskRunResult(taskStatus = FlowStatus.WAITING)
                }

                else -> {
                    runtimeLogger.info(
                        "GitLab pipeline is ready: ${draft.toGitlabReference(iid = mrIid)}, pipeline=${latestPipeline.id}, " +
                            "status=${latestPipeline.status}",
                    )
                    TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
                }
            }
        } catch (exception: Exception) {
            runtimeLogger.warn(
                "GitLab pipeline is not ready: ${draft.toGitlabReference(iid = mrIid)}, error=${exception.message}",
            )
            TaskRunResult(taskStatus = FlowStatus.WAITING)
        }
    }

    private companion object {
        val WAITING_PIPELINE_STATUSES = setOf("running", "pending", "created", "preparing", "waiting_for_resource")
        val RETRYABLE_PIPELINE_STATUSES = setOf("failed", "canceled")
    }
}
