package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.task

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.StageRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.TaskRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.FlowChannels
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.TaskRecoveryBarrier
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.TaskStatusChangedEvent
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.stage.StageStatusService

@Service
class TaskOrchestrator(
    private val channels: FlowChannels,
    private val taskRuntimeService: TaskRuntimeService,
    private val stageRuntimeService: StageRuntimeService,
    private val taskDefinitionResolver: TaskDefinitionResolver,
    private val taskRetryService: TaskRetryService,
    private val taskStatusService: TaskStatusService,
    private val stageStatusService: StageStatusService,
    private val appScope: CoroutineScope,
    private val taskRecoveryBarrier: TaskRecoveryBarrier,
) {

    fun startEventProcessing() {
        appScope.launch { processTaskEvents() }
    }

    private suspend fun processTaskEvents() {
        for (event in channels.taskStatusChangedChannel) {
            handleTaskStatusChanged(event)
        }
    }

    private fun handleTaskStatusChanged(event: TaskStatusChangedEvent) {
        val taskRun = taskRuntimeService.getTaskRun(event.taskRunId)
        when (event.newStatus) {
            FlowStatus.READY -> taskStatusService.recomputeTaskStatus(event.taskRunId)
            FlowStatus.RUNNING, FlowStatus.WAITING -> enqueueTaskRun(taskRun)
            FlowStatus.FAILED_WITH_RETRY -> handleRetryableTask(taskRun)
            FlowStatus.FAILED -> handleFailedTask(taskRun)
            FlowStatus.SUCCEEDED,
            FlowStatus.SKIPPED,
            FlowStatus.CANCELED -> handleCompletedTask(taskRun)
            else -> Unit
        }
    }

    private fun handleRetryableTask(taskRun: TaskRunEntity) {
        when (val decision = taskRetryService.resolveRetryDecision(taskRun, FlowStatus.FAILED_WITH_RETRY)) {
            is TaskRetryDecision.Retry -> {
                val retryTaskRun = taskRuntimeService.incrementTaskRunAttempt(taskRun)
                taskStatusService.updateTaskStatus(retryTaskRun, FlowStatus.PENDING)
                scheduleRetry(retryTaskRun.requireId(), decision.backoff)
            }

            is TaskRetryDecision.Final -> {
                taskStatusService.updateTaskStatus(taskRun, decision.status)
            }
        }
    }

    private fun handleFailedTask(taskRun: TaskRunEntity) {
        if (shouldSkipTaskOnFailure(taskRun)) {
            taskStatusService.updateTaskStatus(taskRun, FlowStatus.SKIPPED)
            return
        }
        stageStatusService.recomputeStageStatus(taskRun.stageRunId)
    }

    private fun handleCompletedTask(taskRun: TaskRunEntity) {
        val stageRun = stageRuntimeService.getStageRun(taskRun.stageRunId)
        if (stageRun.status == FlowStatus.SKIPPED) {
            return
        }

        taskRuntimeService.getDependents(taskRun.requireId())
            .forEach { dependency ->
                taskStatusService.recomputeTaskStatus(dependency.taskRunId)
            }
        stageStatusService.recomputeStageStatus(taskRun.stageRunId)
    }

    suspend fun recoverAfterRestart() {
        taskRuntimeService.getTaskRunsByStatus(FlowStatus.FAILED_WITH_RETRY)
            .forEach(::handleRetryableTask)

        taskRuntimeService.getTaskRunsByStatus(FlowStatus.RUNNING)
            .forEach { taskRun ->
                taskStatusService.updateTaskStatus(taskRun, FlowStatus.FAILED_WITH_RETRY)
            }

        taskRuntimeService.getTaskRunsByStatus(FlowStatus.WAITING)
            .forEach { taskRun ->
                enqueueTaskRun(taskRun)
            }

        taskRuntimeService.getTaskRunsByStatus(FlowStatus.READY)
            .forEach { taskRun ->
                taskStatusService.recomputeTaskStatus(taskRun.requireId())
            }
    }

    private fun scheduleRetry(taskRunId: java.util.UUID, backoff: java.time.Duration) {
        appScope.launch {
            kotlinx.coroutines.delay(backoff.toMillis())
            taskRecoveryBarrier.awaitRecoveryCompleted()
            taskStatusService.recomputeTaskStatus(taskRunId)
        }
    }

    private fun enqueueTaskRun(taskRun: TaskRunEntity) {
        appScope.launch {
            taskRecoveryBarrier.awaitRecoveryCompleted()
            val actualTaskRun = taskRuntimeService.getTaskRun(taskRun.requireId())
            if (actualTaskRun.status !in executionStatuses) {
                return@launch
            }
            val result = channels.taskRunChannel.trySend(actualTaskRun)
            check(result.isSuccess) { "Failed to enqueue taskRunId=${taskRun.requireId()} for execution" }
        }
    }

    private fun shouldSkipTaskOnFailure(taskRun: TaskRunEntity): Boolean {
        return taskDefinitionResolver.getTaskDefinition(taskRun).skipOnFailed
    }

    private companion object {
        val executionStatuses = setOf(
            FlowStatus.RUNNING,
            FlowStatus.WAITING,
        )
    }
}
