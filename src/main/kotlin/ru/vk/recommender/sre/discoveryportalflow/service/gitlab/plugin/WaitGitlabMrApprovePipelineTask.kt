package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowWaitingTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabMergeRequestTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.utils.toGitlabReference

class WaitGitlabMrApprovePipelineTask(
    private val gitlabClient: GitlabClient,
) : FlowWaitingTask<GitlabMergeRequestTaskContext>(GitlabMergeRequestTaskContext::class) {

    override suspend fun check(taskRunContext: GitlabMergeRequestTaskContext): TaskRunResult {
        val draft = taskRunContext.requireDraft()
        val mrIid = taskRunContext.requireMergeRequestIid()

        try {
            val mergeRequestDescription = gitlabClient.getMergeRequestDescription(
                draft.projectId,
                mrIid
            )
            val description = mergeRequestDescription?.split("\n")
            if (description == null) {
                runtimeLogger.warn("No description for MR: ${taskRunContext.gitlabMergeRequestUrl}")
                return TaskRunResult(taskStatus = FlowStatus.WAITING)
            }

            val codeReviewStarted = description.contains(CODEREVIEW_ANCHOR)
            if (!codeReviewStarted) {
                runtimeLogger.warn("Code review not started for MR: ${taskRunContext.gitlabMergeRequestUrl}")
                return TaskRunResult(taskStatus = FlowStatus.WAITING)
            }

            val rules = description.filter { it.startsWith(RULE_ID_PREFIX) }
            val approvedRules = rules.filter { it.endsWith(OK_POSTFIX) && !it.contains(OPTIONAL_RULE) }
            val notApprovedRules = rules.filter { !it.endsWith(OK_POSTFIX) }

            if (approvedRules.isEmpty()) {
                gitlabClient.approveMergeRequest(
                    projectId = draft.projectId,
                    mrIid = mrIid
                )
                runtimeLogger.info(
                    "Approved GitLab merge request: ${draft.toGitlabReference(iid = mrIid)}, url=${taskRunContext.gitlabMergeRequestUrl}",
                )
                return TaskRunResult(taskStatus = FlowStatus.WAITING)
            }

            if (notApprovedRules.isNotEmpty()) {
                runtimeLogger.error("Cant approve rules for MR ${taskRunContext.gitlabMergeRequestUrl}\nrules:$notApprovedRules")
                return TaskRunResult(taskStatus = FlowStatus.FAILED)
            }

            val status = description.filter { it.startsWith(STATUS_PREFIX) }
            if (status.size != 1) {
                runtimeLogger.error("Error parsing MR(${taskRunContext.gitlabMergeRequestUrl}) code review status: $status")
                return TaskRunResult(taskStatus = FlowStatus.FAILED)
            }

            if (status[0].contains(STATUS_APPROVED) && notApprovedRules.isEmpty()) {
                runtimeLogger.info("Done approving MR=${taskRunContext.gitlabMergeRequestUrl}")
                return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
            }

            runtimeLogger.warn("Unknown status for MR=${taskRunContext.gitlabMergeRequestUrl}\nApproved rules = $approvedRules\nNot approved = $notApprovedRules\nStatus=$status")
            return TaskRunResult(taskStatus = FlowStatus.WAITING)
        } catch (exception: Exception) {
            runtimeLogger.warn(
                "GitLab code review is not ready: ${taskRunContext.gitlabMergeRequestUrl}, error=${exception.message}",
            )
            return TaskRunResult(taskStatus = FlowStatus.WAITING)
        }
    }

    private companion object {
        private const val CODEREVIEW_ANCHOR = "<!-- BEGIN CODEREVIEW REPORT -->"
        private const val RULE_ID_PREFIX = "- Rule Id ["
        private const val OK_POSTFIX = ":white_check_mark:"
        private const val STATUS_PREFIX = "### Status: "
        private const val STATUS_APPROVED = "approved"
        private const val OPTIONAL_RULE = "Optional. **Approves count 0**"
    }
}
