package ru.vk.recommender.sre.discoveryportalflow.service.engine.logging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.LogType
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskLogEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.repository.TaskLogRepository
import ru.vk.recommender.sre.discoveryportalflow.persistence.repository.TaskRunRepository
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.execution.TaskExecutionContextHolder
import java.util.UUID

@Component
class TasksRuntimeLogger(
    private val taskLogRepository: TaskLogRepository,
    private val taskRunRepository: TaskRunRepository,
    private val taskExecutionContextHolder: TaskExecutionContextHolder,
) {

    fun logStatusChange(taskRunId: UUID, status: FlowStatus) {
        taskLogRepository.save(
            TaskLogEntity(
                taskRunId = taskRunId,
                status = status,
                type = LogType.INFO,
                message = "Task status changed to $status",
            )
        )
    }

    suspend fun info(message: String) = log(LogType.INFO, message)

    suspend fun warn(message: String) = log(LogType.WARN, message)

    suspend fun error(message: String) = log(LogType.ERROR, message)

    suspend fun logFailure(exception: Throwable) {
        val message = buildFailureMessage(exception)
        log(LogType.ERROR, message)
    }

    suspend fun logFailure(taskRunId: UUID, exception: Throwable) {
        val message = buildFailureMessage(exception)
        log(taskRunId, LogType.ERROR, message)
    }

    private suspend fun log(type: LogType, message: String) {
        val taskRunId = currentTaskRunId()
        log(taskRunId, type, message)
    }

    private suspend fun log(taskRunId: UUID, type: LogType, message: String) {
        withContext(Dispatchers.IO) {
            val status = taskRunRepository.findByIdOrNull(taskRunId)?.status ?: kotlin.error("Can't find task run with id=$taskRunId")
            taskLogRepository.save(
                TaskLogEntity(
                    taskRunId = taskRunId,
                    status = status,
                    type = type,
                    message = message,
                )
            )
        }
    }

    private suspend fun currentTaskRunId(): UUID {
        return taskExecutionContextHolder.current().taskRunId
    }

    private fun buildFailureMessage(exception: Throwable): String {
        val details = exception.message ?: "no message"
        val typeName = exception::class.simpleName ?: "Throwable"
        return "Task failed with exception $typeName: $details"
    }

}
