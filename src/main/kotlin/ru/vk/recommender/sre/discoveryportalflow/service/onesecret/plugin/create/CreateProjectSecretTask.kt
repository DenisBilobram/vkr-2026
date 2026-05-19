package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.create

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.service.OneSecretService

class CreateProjectSecretTask(
    private val oneSecretService: OneSecretService,
) : FlowTask<OneSecretTaskContext>(OneSecretTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: OneSecretTaskContext): TaskRunResult {
        val projectName = taskRunContext.projectName
        if (projectName == null) {
            runtimeLogger.info("Skip project secrets: recommender has no project")
            return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
        }

        val projectProductId = requireNotNull(taskRunContext.projectProductId) {
            "recommender.projectProductId is required to configure project secrets for $projectName"
        }
        val apptracerToken = requireNotBlank(taskRunContext.apptracerToken, "oneSecret.apptracerToken")

        val secretId = oneSecretService.configureProjectSecret(
            projectName = projectName,
            projectProductId = projectProductId,
            recommenderName = taskRunContext.recommenderName,
            apptracerToken = apptracerToken,
            ytOffline = taskRunContext.ytOffline,
            queueTargets = taskRunContext.projectQueueTargets,
        )
        taskRunContext.projectOneSecretId = secretId

        runtimeLogger.info(
            "Configured project secrets for $projectName: secretId=$secretId, " +
                "sharedQueues=${taskRunContext.projectQueueTargets.size}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    private fun requireNotBlank(value: String?, fieldName: String): String {
        return value?.takeIf { it.isNotBlank() }
            ?: error("$fieldName is required to configure project secrets")
    }
}
