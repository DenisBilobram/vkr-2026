package ru.vk.recommender.sre.discoveryportalflow.service.hermes.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.context.HermesSnapshotCopyTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.service.HermesSnapshotService

class BuildProdHermesSnapshotsTask(
    private val hermesSnapshotService: HermesSnapshotService,
) : FlowTask<HermesSnapshotCopyTaskContext>(HermesSnapshotCopyTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: HermesSnapshotCopyTaskContext): TaskRunResult {
        val result = hermesSnapshotService.triggerProdSnapshotBuilds(taskRunContext)
        runtimeLogger.info(
            "Triggered ${result.triggeredRequestsCount} Hermes prod snapshot builds for " +
                "${taskRunContext.recommenderName} via ${result.builderBaseUrl}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
