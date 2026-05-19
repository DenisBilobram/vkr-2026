package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.create

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.service.OneSecretService

class CreateVerticalSecretTask(
    private val oneSecretService: OneSecretService,
) : FlowTask<OneSecretTaskContext>(OneSecretTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: OneSecretTaskContext): TaskRunResult {
        val productId = requireNotNull(taskRunContext.productId) {
            "recommender.productId is required to configure vertical secrets for ${taskRunContext.recommenderName}"
        }
        val apptracerToken = requireNotBlank(taskRunContext.apptracerToken, "oneSecret.apptracerToken")

        val secretId = oneSecretService.configureVerticalSecrets(
            recommenderName = taskRunContext.recommenderName,
            projectName = taskRunContext.projectName,
            productId = productId,
            projectProductId = taskRunContext.projectProductId,
            apptracerToken = apptracerToken,
            queueTargets = taskRunContext.verticalQueueTargets,
        )
        taskRunContext.recomOneSecretId = secretId

        runtimeLogger.info(
            "Configured vertical secrets for ${taskRunContext.recommenderName}: secretId=$secretId, " +
                "sharedQueues=${taskRunContext.verticalQueueTargets.size}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    private fun requireNotBlank(value: String?, fieldName: String): String {
        return value?.takeIf { it.isNotBlank() }
            ?: error("$fieldName is required to configure vertical secrets")
    }
}
