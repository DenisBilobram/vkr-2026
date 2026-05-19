package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.plugin

import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.DatacenterCode
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.client.OnecloudClient
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.client.OnecloudDatacenterResponse
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context.OnecloudSubmitStoragesTaskContext

class OnecloudSubmitStoragesTask(
    private val onecloudClient: OnecloudClient,
) : FlowTask<OnecloudSubmitStoragesTaskContext>(OnecloudSubmitStoragesTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: OnecloudSubmitStoragesTaskContext): TaskRunResult {
        val storageSubmissions = taskRunContext.storageSubmissions
        if (storageSubmissions.isEmpty()) {
            runtimeLogger.info("No OneCloud storage submissions configured")
            return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
        }

        runtimeLogger.info("Submitting ${storageSubmissions.size} OneCloud storages")
        storageSubmissions.forEachIndexed { index, submission ->
            require(submission.storageJson.isNotBlank()) {
                "OneCloud storage submission at index $index must set storageJson"
            }
            require(submission.dcs.isNotEmpty()) {
                "OneCloud storage submission at index $index must set dcs"
            }

            val result = onecloudClient.submitStorageIfAbsent(
                storageJson = submission.storageJson,
                queue = submission.queue,
                shards = submission.shards,
                dcs = submission.dcs,
            )
            if (result.submitted.isNotEmpty()) {
                runtimeLogger.info(
                    "Submitted OneCloud storage ${index + 1}/${storageSubmissions.size} to ${result.submitted.submittedDcsLog()}",
                )
            }
            if (result.skippedExistingDcs.isNotEmpty()) {
                runtimeLogger.info(
                    "Skipped existing OneCloud storage ${index + 1}/${storageSubmissions.size} in ${result.skippedExistingDcs.dcsLog()}",
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
