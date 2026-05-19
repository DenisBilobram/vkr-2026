package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.plugin

import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.DatacenterCode
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.client.OnecloudClient
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.client.OnecloudDatacenterResponse
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context.OnecloudSubmitQueuesTaskContext

class OnecloudSubmitQueuesTask(
    private val onecloudClient: OnecloudClient,
) : FlowTask<OnecloudSubmitQueuesTaskContext>(OnecloudSubmitQueuesTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: OnecloudSubmitQueuesTaskContext): TaskRunResult {
        val queueSubmissions = taskRunContext.queueSubmissions
        if (queueSubmissions.isEmpty()) {
            runtimeLogger.info("No OneCloud queue submissions configured")
            return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
        }

        runtimeLogger.info("Submitting ${queueSubmissions.size} OneCloud queues")
        queueSubmissions.forEachIndexed { index, submission ->
            require(submission.queueJson.isNotBlank()) {
                "OneCloud queue submission at index $index must set queueJson"
            }
            require(submission.dcs.isNotEmpty()) {
                "OneCloud queue submission at index $index must set dcs"
            }

            val result = onecloudClient.submitQueueIfAbsent(
                queueJson = submission.queueJson,
                user = submission.user,
                dcs = submission.dcs,
            )
            if (result.submitted.isNotEmpty()) {
                runtimeLogger.info(
                    "Submitted OneCloud queue ${index + 1}/${queueSubmissions.size} to ${result.submitted.submittedDcsLog()}",
                )
            }
            if (result.skippedExistingDcs.isNotEmpty()) {
                runtimeLogger.info(
                    "Skipped existing OneCloud queue ${index + 1}/${queueSubmissions.size} in ${result.skippedExistingDcs.dcsLog()}",
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
