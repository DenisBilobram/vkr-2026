package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabWebhookTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.config.TogglesProperties

class CreateGitlabWebhookTask(
    private val gitlabClient: GitlabClient,
    private val togglesProperties: TogglesProperties,
) : FlowTask<GitlabWebhookTaskContext>(GitlabWebhookTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: GitlabWebhookTaskContext): TaskRunResult {
        val webhookInfo = taskRunContext.gitlabWebhookInfo
        val projectId = requireNotNull(
            gitlabClient.getProjectByPath(webhookInfo.gitlabRepositoryPath)?.id,
        ) {
            "GitLab project '${webhookInfo.gitlabRepositoryPath}' is required to create webhook"
        }

        if (gitlabClient.hasWebhook(projectId, webhookInfo.gitlabWebhookUrl)) {
            runtimeLogger.info(
                "GitLab webhook already exists for project $projectId: ${webhookInfo.gitlabWebhookUrl}",
            )
            return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
        }

        gitlabClient.createWebhook(
            projectId = projectId,
            webhookUrl = webhookInfo.gitlabWebhookUrl,
            webhookToken = requireWebhookToken(),
            mergeRequestsEvents = true,
            pushEvents = false,
            enableSslVerification = false,
        )
        runtimeLogger.info(
            "Created GitLab webhook for project $projectId: ${webhookInfo.gitlabWebhookUrl}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    private fun requireWebhookToken(): String {
        return togglesProperties.webhookToken
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: error("toggles.webhook-token must be configured to create tenant webhooks")
    }
}
