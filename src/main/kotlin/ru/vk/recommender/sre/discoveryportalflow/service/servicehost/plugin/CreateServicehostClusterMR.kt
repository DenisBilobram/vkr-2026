package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.context.ServicehostTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.service.mr.ServicehostClusterMrService

class CreateServicehostClusterMR(
    private val servicehostClusterMrService: ServicehostClusterMrService,
) : FlowTask<ServicehostTaskContext>(ServicehostTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: ServicehostTaskContext): TaskRunResult {
        val clusterInitializationResult = servicehostClusterMrService.createClusterMrIfNeeded(taskRunContext)

        if (clusterInitializationResult.createdNow) {
            runtimeLogger.info(
                "Created servicehost cluster for ${taskRunContext.recommenderName}: " +
                    "${clusterInitializationResult.fullClusterName}, mrUrl=${clusterInitializationResult.clusterMrUrl}",
            )
        } else {
            runtimeLogger.info(
                "Servicehost cluster already exists for ${taskRunContext.recommenderName}: " +
                    "${clusterInitializationResult.fullClusterName}, cluster creation skipped",
            )
        }

        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
