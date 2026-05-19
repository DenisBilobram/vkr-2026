package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.create

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.service.OneSecretProvisioningService

class CreateServiceSecretTask(
    private val oneSecretProvisioningService: OneSecretProvisioningService,
) : FlowTask<OneSecretTaskContext>(OneSecretTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: OneSecretTaskContext): TaskRunResult {
        val configuredServices = mutableListOf<String>()

        taskRunContext.serviceTargets.forEach { serviceTarget ->
            val secretOutcome = oneSecretProvisioningService.configureServiceSecrets(
                taskContext = taskRunContext,
                serviceTarget = serviceTarget,
            )

            val cloudServiceName = serviceTarget.serviceRuntime.cloudServiceName
            if (secretOutcome.secretCreated) {
                taskRunContext.serviceOneSecretOutcomes[cloudServiceName] = secretOutcome
                configuredServices += cloudServiceName
            } else {
                taskRunContext.serviceOneSecretOutcomes.remove(cloudServiceName)
            }
        }

        runtimeLogger.info(
            "Configured service secrets for ${taskRunContext.recommenderName}: services=${configuredServices.size}",
        )
        if (configuredServices.isNotEmpty()) {
            runtimeLogger.info("Service secret services: ${configuredServices.joinToString(", ")}")
        }

        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
