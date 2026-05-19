package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowWaitingTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.client.OnecloudClient
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context.OnecloudWaitServicesRunningTaskContext

class OnecloudWaitServicesCreatedTask(
    private val onecloudClient: OnecloudClient,
) : FlowWaitingTask<OnecloudWaitServicesRunningTaskContext>(OnecloudWaitServicesRunningTaskContext::class) {

    override suspend fun check(taskRunContext: OnecloudWaitServicesRunningTaskContext): TaskRunResult {
        val createdServices = mutableListOf<String>()
        val missingServices = mutableListOf<String>()
        var totalServices = 0

        taskRunContext.serviceTargets.forEachIndexed { index, service ->
            val serviceName = service.name.trim()
            if (serviceName.isBlank()) {
                return@forEachIndexed
            }
            require(service.dcs.isNotEmpty()) {
                "OneCloud service target at index $index must set dcs"
            }

            onecloudClient.serviceInfoExists(
                serviceName = serviceName,
                dcs = service.dcs,
            ).forEach { result ->
                totalServices += 1
                val serviceLogName = "$serviceName@${result.dc.name.uppercase()}"
                if (result.response) {
                    createdServices.add(serviceLogName)
                } else {
                    missingServices.add(serviceLogName)
                }
            }
        }

        if (totalServices == 0) {
            runtimeLogger.info("No OneCloud services configured for creation waiting")
            return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
        }

        if (missingServices.isEmpty()) {
            runtimeLogger.info("All OneCloud services are created: ${createdServices.joinToString()}")
            return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
        }

        runtimeLogger.info(
            "Waiting for OneCloud services to appear: created=${createdServices.size}, missing=${missingServices.size}, total=$totalServices",
        )
        return TaskRunResult(taskStatus = FlowStatus.WAITING)
    }
}
