package ru.vk.recommender.sre.discoveryportalflow.service.hermes.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.context.HermesSnapshotCopyTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.service.HermesSnapshotService

class CopyHermesSnapshotMetaTask(
    private val hermesSnapshotService: HermesSnapshotService,
) : FlowTask<HermesSnapshotCopyTaskContext>(HermesSnapshotCopyTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: HermesSnapshotCopyTaskContext): TaskRunResult {
        require(taskRunContext.resolvedProdSnapshots.isNotEmpty()) {
            "Resolved prod Hermes snapshots are required before copy"
        }

        val copyResult = hermesSnapshotService.copySnapshotsMetaToTesting(taskRunContext.resolvedProdSnapshots)
        runtimeLogger.info(
            "Synced Hermes snapshot metas to testing for ${taskRunContext.recommenderName}: " +
                "resolved=${taskRunContext.resolvedProdSnapshots.size}, copied=${copyResult.copiedCount}, " +
                "skipped=${copyResult.skippedCount}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
