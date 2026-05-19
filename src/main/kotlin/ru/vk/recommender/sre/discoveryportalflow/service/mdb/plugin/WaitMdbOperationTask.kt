package ru.vk.recommender.sre.discoveryportalflow.service.mdb.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowWaitingTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.context.MdbTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.service.MdbMongoDbService

class WaitMdbOperationTask(
    private val mdbMongoDbService: MdbMongoDbService,
) : FlowWaitingTask<MdbTaskContext>(MdbTaskContext::class) {

    override suspend fun check(taskRunContext: MdbTaskContext): TaskRunResult {
        val operationId = requireNotNull(taskRunContext.pendingOperationId) {
            "MDB operation id is not set in context"
        }
        val operationDescription = taskRunContext.pendingOperationDescription ?: "MDB operation '$operationId'"
        val operation = mdbMongoDbService.findOperation(operationId)

        if (operation == null) {
            runtimeLogger.info("Waiting for $operationDescription: operation '$operationId' is not visible yet")
            return TaskRunResult(taskStatus = FlowStatus.WAITING)
        }

        return when (operation.status.lowercase()) {
            MdbMongoDbService.STATUS_DONE -> {
                taskRunContext.pendingOperationId = null
                taskRunContext.pendingOperationDescription = null
                runtimeLogger.info("Completed $operationDescription: operation '$operationId'")
                TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
            }

            MdbMongoDbService.STATUS_FAILED,
            MdbMongoDbService.STATUS_CANCELED -> error(
                "MDB operation failed: action=$operationDescription id=${operation.id} " +
                    "status=${operation.status} error=${operation.errorMessage ?: "empty"}",
            )

            else -> {
                runtimeLogger.info(
                    "Waiting for $operationDescription: operation '$operationId' status=${operation.status}",
                )
                TaskRunResult(taskStatus = FlowStatus.WAITING)
            }
        }
    }
}
