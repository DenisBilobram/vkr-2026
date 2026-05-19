package ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.TaskRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.context.FlowContextManager
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.context.FlowContextMerger
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.FlowChannels
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.task.TaskDefinitionResolver
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.task.TaskStatusService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.logging.TasksRuntimeLogger
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.exception.RetryableException

@Service
class TaskWorker(
    private val flowContextManager: FlowContextManager,
    private val flowContextMerger: FlowContextMerger,
    private val channels: FlowChannels,
    private val taskRunner: TaskRunner,
    private val taskDefinitionResolver: TaskDefinitionResolver,
    private val tasksRuntimeLogger: TasksRuntimeLogger,
    private val taskRuntimeService: TaskRuntimeService,
    private val taskStatusService: TaskStatusService,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun startExecution() {
        scope.launch { executeTasks() }
    }

    private suspend fun executeTasks() {
        for (taskRun in channels.taskRunChannel) {
            scope.launch {
                val actualTaskRun = taskRuntimeService.getTaskRun(taskRun.requireId())
                if (actualTaskRun.status !in activeExecutionStatuses) {
                    return@launch
                }
                val result = runTaskWithFlowContext(actualTaskRun)
                taskStatusService.updateTaskStatus(actualTaskRun.requireId(), result.taskStatus)
            }
        }
    }

    private suspend fun runTaskWithFlowContext(taskRun: TaskRunEntity): TaskRunResult {
        return runCatching {
            val flowContext = flowContextManager.loadFlowContextByStageRunId(taskRun.stageRunId)
            val taskDefinition = taskDefinitionResolver.getTaskDefinition(taskRun)
            val taskResult = taskRunner.run(
                taskRun = taskRun,
                taskDefinition = taskDefinition,
                contextJson = flowContext,
            )
            val mergedFlowContext = flowContextMerger.merge(flowContext, taskResult.updatedContext)
            flowContextManager.saveFlowContextByStageRunId(taskRun.stageRunId, mergedFlowContext)
            TaskRunResult(taskStatus = taskResult.taskStatus)
        }.getOrElse { exception ->
            tasksRuntimeLogger.logFailure(taskRun.requireId(), exception)
            if (exception is RetryableException) {
                return TaskRunResult(taskStatus = FlowStatus.FAILED_WITH_RETRY)
            }
            return TaskRunResult(taskStatus = FlowStatus.FAILED)
        }
    }

    private companion object {
        val activeExecutionStatuses = setOf(
            FlowStatus.RUNNING,
            FlowStatus.WAITING,
        )
    }
}
