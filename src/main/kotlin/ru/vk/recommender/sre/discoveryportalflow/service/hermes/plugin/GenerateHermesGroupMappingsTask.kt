package ru.vk.recommender.sre.discoveryportalflow.service.hermes.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.context.HermesSnapshotCopyTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.service.HermesSnapshotService

class GenerateHermesGroupMappingsTask(
    private val hermesSnapshotService: HermesSnapshotService,
) : FlowTask<HermesSnapshotCopyTaskContext>(HermesSnapshotCopyTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: HermesSnapshotCopyTaskContext): TaskRunResult {
        val snapshotServices = taskRunContext.services
            .filter { serviceRuntime -> serviceRuntime.hasSnapshots }

        snapshotServices.forEach { serviceRuntime ->
            val addMapping = hermesSnapshotService.addMapping(serviceRuntime)
            if (addMapping) {
                runtimeLogger.info("Successfully add hermes mapping for service ${serviceRuntime.cloudServiceName}")
            } else {
                throw RuntimeException("Failed to add mapping for service ${serviceRuntime.cloudServiceName}")
            }
        }
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
