package ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common

import kotlinx.coroutines.delay
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.TaskDefinitionProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import kotlin.reflect.KClass

abstract class FlowWaitingTask<ContextType : FlowTaskContext>(
    contextClass: KClass<ContextType>,
) : FlowTask<ContextType>(contextClass) {

    protected abstract suspend fun check(taskRunContext: ContextType): TaskRunResult

    override suspend fun executeCasted(taskRunContext: ContextType): TaskRunResult {
        val taskDefinition = currentTaskDefinition()
        validateWaitingDefinition(taskDefinition)
        var taskResult = check(taskRunContext)
        while (taskResult.taskStatus == FlowStatus.WAITING) {
            delay(taskDefinition.waitingPolicy.delay.toMillis())
            taskResult = check(taskRunContext)
        }
        return taskResult
    }

    private suspend fun validateWaitingDefinition(taskDefinition: TaskDefinitionProperties) {
        val taskName = currentTaskName()
        require(taskDefinition.waitingPolicy.waitingTask) {
            "Task $taskName must set waiting-policy.waiting-task: true in task definitions"
        }
        require(taskDefinition.timeout.toMillis() > 0) {
            "Task $taskName must set a positive timeout in task definitions"
        }
        require(taskDefinition.waitingPolicy.delay.toMillis() > 0) {
            "Task $taskName must set a positive waiting-policy.delay in task definitions"
        }
    }
}
