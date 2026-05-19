package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.task

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.StageRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.TaskRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.TaskRecoveryBarrier
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.stage.StageStatusService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.validation.FlowRunValidator
import java.time.Duration
import java.util.UUID

@Service
class TaskRetryService(
    private val scope: CoroutineScope,
    private val taskRuntimeService: TaskRuntimeService,
    private val stageRuntimeService: StageRuntimeService,
    private val taskDefinitionResolver: TaskDefinitionResolver,
    private val flowRunValidator: FlowRunValidator,
    private val taskStatusService: TaskStatusService,
    private val stageStatusService: StageStatusService,
    private val taskRecoveryBarrier: TaskRecoveryBarrier,
) {

    fun retryTask(taskRunId: UUID): TaskRunEntity {
        val taskRun = taskRuntimeService.getTaskRun(taskRunId)
        flowRunValidator.requireTaskCanRetry(taskRun)

        val stageRun = stageRuntimeService.getStageRun(taskRun.stageRunId)
        val stageWasRunning = stageRun.status == FlowStatus.RUNNING
        if (!stageWasRunning) {
            stageStatusService.updateStageStatus(stageRun, FlowStatus.RUNNING)
        }

        val retryTaskRun = taskRuntimeService.incrementTaskRunAttempt(taskRun)
        taskStatusService.updateTaskStatus(retryTaskRun, FlowStatus.PENDING)
        scope.launch {
            taskRecoveryBarrier.awaitRecoveryCompleted()
            taskStatusService.recomputeTaskStatus(retryTaskRun.requireId())
        }
        return taskRuntimeService.getTaskRun(retryTaskRun.requireId())
    }

    fun resolveRetryDecision(taskRun: TaskRunEntity, taskStatus: FlowStatus): TaskRetryDecision {
        if (taskStatus != FlowStatus.FAILED_WITH_RETRY) {
            return TaskRetryDecision.Final(taskStatus)
        }

        val retryPolicy = taskDefinitionResolver.getTaskDefinition(taskRun).retryPolicy
        if (!retryPolicy.autoRetryEnabled) {
            return TaskRetryDecision.Final(FlowStatus.FAILED)
        }

        val lastAttemptIndex = retryPolicy.attempts - 1
        return if (taskRun.attemptNumber >= lastAttemptIndex) {
            TaskRetryDecision.Final(FlowStatus.FAILED)
        } else {
            TaskRetryDecision.Retry(retryPolicy.backoff)
        }
    }
}

sealed class TaskRetryDecision {
    data class Retry(val backoff: Duration) : TaskRetryDecision()
    data class Final(val status: FlowStatus) : TaskRetryDecision()
}
