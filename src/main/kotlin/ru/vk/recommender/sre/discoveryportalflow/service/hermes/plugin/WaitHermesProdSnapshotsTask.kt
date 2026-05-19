package ru.vk.recommender.sre.discoveryportalflow.service.hermes.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowWaitingTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.context.HermesSnapshotCopyTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.service.HermesSnapshotService

class WaitHermesProdSnapshotsTask(
    private val hermesSnapshotService: HermesSnapshotService,
) : FlowWaitingTask<HermesSnapshotCopyTaskContext>(HermesSnapshotCopyTaskContext::class) {

    override suspend fun check(taskRunContext: HermesSnapshotCopyTaskContext): TaskRunResult {
        val resolution = hermesSnapshotService.resolveProdSnapshots(taskRunContext.snapshotTypeIds)
        if (resolution.missingTypeIds.isNotEmpty()) {
            val preview = resolution.missingTypeIds.take(5).joinToString()
            val suffix = if (resolution.missingTypeIds.size > 5) ", ..." else ""
            runtimeLogger.info(
                "Waiting for ${resolution.missingTypeIds.size}/${taskRunContext.snapshotTypeIds.size} " +
                    "Hermes prod snapshots for ${taskRunContext.recommenderName}: $preview$suffix",
            )
            return TaskRunResult(taskStatus = FlowStatus.WAITING)
        }

        taskRunContext.resolvedProdSnapshots = resolution.resolvedSnapshots
        runtimeLogger.info(
            "Resolved ${taskRunContext.resolvedProdSnapshots.size} prod Hermes snapshots for " +
                "${taskRunContext.recommenderName}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
