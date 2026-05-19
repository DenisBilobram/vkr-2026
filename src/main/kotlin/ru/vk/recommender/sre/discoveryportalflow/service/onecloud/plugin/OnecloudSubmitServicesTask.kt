package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.plugin

import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.DatacenterCode
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.client.OnecloudClient
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.client.OnecloudDatacenterResponse
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context.OnecloudSubmitServicesTaskContext

class OnecloudSubmitServicesTask(
    private val onecloudClient: OnecloudClient,
) : FlowTask<OnecloudSubmitServicesTaskContext>(OnecloudSubmitServicesTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: OnecloudSubmitServicesTaskContext): TaskRunResult {
        val serviceSubmissions = taskRunContext.serviceSubmissions
        if (serviceSubmissions.isEmpty()) {
            runtimeLogger.info("No OneCloud service submissions configured")
            return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
        }

        runtimeLogger.info("Submitting ${serviceSubmissions.size} OneCloud services")
        serviceSubmissions.forEachIndexed { index, submission ->
            require(submission.serviceJson.isNotBlank()) {
                "OneCloud service submission at index $index must set serviceJson"
            }
            require(submission.dcs.isNotEmpty()) {
                "OneCloud service submission at index $index must set dcs"
            }

            val result = onecloudClient.submitServiceIfAbsent(
                serviceJson = submission.serviceJson,
                queue = submission.queue,
                replicas = submission.replicas,
                minRunning = submission.minRunning,
                pause = submission.pause,
                dcs = submission.dcs,
            )
            if (result.submitted.isNotEmpty()) {
                runtimeLogger.info(
                    "Submitted OneCloud service ${index + 1}/${serviceSubmissions.size} to ${result.submitted.submittedDcsLog()}",
                )
            }
            if (result.skippedExistingDcs.isNotEmpty()) {
                runtimeLogger.info(
                    "Skipped existing OneCloud service ${index + 1}/${serviceSubmissions.size} in ${result.skippedExistingDcs.dcsLog()}",
                )
            }
        }

        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    private fun List<OnecloudDatacenterResponse<*>>.submittedDcsLog(): String {
        return joinToString { response -> response.dc.name.uppercase() }
    }

    private fun List<DatacenterCode>.dcsLog(): String {
        return joinToString { dc -> dc.name.uppercase() }
    }
}
