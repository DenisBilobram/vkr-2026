package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowWaitingTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.client.OnecloudClient
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.client.OnecloudServiceStatus
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context.OnecloudWaitServicesRunningTaskContext

class OnecloudWaitServicesRunningTask(
    private val onecloudClient: OnecloudClient,
) : FlowWaitingTask<OnecloudWaitServicesRunningTaskContext>(OnecloudWaitServicesRunningTaskContext::class) {

    override suspend fun check(taskRunContext: OnecloudWaitServicesRunningTaskContext): TaskRunResult {
        val runningServices = mutableListOf<String>()
        val startingServices = mutableListOf<String>()
        val failedServices = mutableListOf<String>()
        var totalServices = 0

        taskRunContext.serviceTargets.forEachIndexed { index, service ->
            val serviceName = service.name.trim()
            if (serviceName.isBlank()) {
                return@forEachIndexed
            }
            require(service.dcs.isNotEmpty()) {
                "OneCloud service target at index $index must set dcs"
            }

            onecloudClient.getServiceStatus(
                serviceName = serviceName,
                dcs = service.dcs,
            ).forEach { result ->
                totalServices += 1
                val serviceLogName = "$serviceName@${result.dc.name.uppercase()}"
                when (result.response) {
                    OnecloudServiceStatus.RUNNING -> runningServices.add(serviceLogName)
                    OnecloudServiceStatus.STARTING -> startingServices.add(serviceLogName)
                    OnecloudServiceStatus.FAILED -> failedServices.add(serviceLogName)
                }
            }
        }

        if (totalServices == 0) {
            runtimeLogger.info("No OneCloud services configured for waiting")
            return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
        }

        if (failedServices.isNotEmpty()) {
            runtimeLogger.error("OneCloud services failed: ${failedServices.joinToString()}")
            return TaskRunResult(taskStatus = FlowStatus.FAILED)
        }

        if (runningServices.size == totalServices) {
            runtimeLogger.info("All OneCloud services are running: ${runningServices.joinToString()}")
            return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
        }

        runtimeLogger.info(
            "Waiting for OneCloud services: running=${runningServices.size}, starting=${startingServices.size}, total=$totalServices",
        )
        return TaskRunResult(taskStatus = FlowStatus.WAITING)
    }
}
