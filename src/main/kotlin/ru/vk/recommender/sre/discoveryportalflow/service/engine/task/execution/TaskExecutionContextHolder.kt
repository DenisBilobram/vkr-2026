package ru.vk.recommender.sre.discoveryportalflow.service.engine.task.execution

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskRunEntity
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.TaskDefinitionProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.validation.FlowRunValidator
import java.time.Instant

@Component
class TaskExecutionContextHolder(
    private val flowRunValidator: FlowRunValidator,
) {

    suspend fun <T> withTaskRun(
        taskRun: TaskRunEntity,
        taskBeanName: String,
        taskDefinition: TaskDefinitionProperties,
        block: suspend () -> T
    ): T {
        val taskRunId = taskRun.requireId()
        val startedAt = Instant.ofEpochSecond(flowRunValidator.requireStartedTaskRun(taskRun))
        val context = TaskExecutionContext(
            taskRunId = taskRunId,
            taskName = taskRun.taskName,
            taskBeanName = taskBeanName,
            taskDefinition = taskDefinition,
            stageRunId = taskRun.stageRunId,
            attemptNumber = taskRun.attemptNumber,
            startedAt = startedAt,
        )
        return withContext(context) { block() }
    }

    suspend fun current(): TaskExecutionContext {
        return currentCoroutineContext()[TaskExecutionContext]
            ?: error("Task execution context is missing")
    }
}
