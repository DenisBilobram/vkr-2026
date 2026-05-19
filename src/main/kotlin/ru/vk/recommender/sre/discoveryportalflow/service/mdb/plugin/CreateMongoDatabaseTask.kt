package ru.vk.recommender.sre.discoveryportalflow.service.mdb.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.context.MdbTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.service.MdbMongoDbService

class CreateMongoDatabaseTask(
    private val mdbMongoDbService: MdbMongoDbService,
) : FlowTask<MdbTaskContext>(MdbTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: MdbTaskContext): TaskRunResult {
        taskRunContext.pendingOperationId = mdbMongoDbService.createDatabase(taskRunContext.databaseName)
        taskRunContext.pendingOperationDescription = "create database '${taskRunContext.databaseName}'"
        runtimeLogger.info(
            "Started MDB operation ${taskRunContext.pendingOperationId} to create database '${taskRunContext.databaseName}'",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
