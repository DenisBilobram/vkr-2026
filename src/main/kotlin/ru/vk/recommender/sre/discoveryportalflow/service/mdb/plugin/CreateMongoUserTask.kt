package ru.vk.recommender.sre.discoveryportalflow.service.mdb.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.context.MdbTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.service.MdbMongoDbService

class CreateMongoUserTask(
    private val mdbMongoDbService: MdbMongoDbService,
) : FlowTask<MdbTaskContext>(MdbTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: MdbTaskContext): TaskRunResult {
        taskRunContext.pendingOperationId = mdbMongoDbService.createUser(
            databaseName = taskRunContext.databaseName,
            userName = taskRunContext.userName,
            userPassword = taskRunContext.userPassword,
        )
        taskRunContext.pendingOperationDescription = "create user '${taskRunContext.userName}'"
        runtimeLogger.info(
            "Started MDB operation ${taskRunContext.pendingOperationId} to create user '${taskRunContext.userName}' for database '${taskRunContext.databaseName}'",
        )
        runtimeLogger.info("Generated MDB password for '${taskRunContext.userName}': ${taskRunContext.userPassword}")
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
