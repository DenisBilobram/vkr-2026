package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.task

import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.StageRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.TaskRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.FlowStatusEventPublisher
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration.TaskStatusChangedEvent
import java.util.UUID

@Service
class TaskStatusService(
    private val taskRuntimeService: TaskRuntimeService,
    private val stageRuntimeService: StageRuntimeService,
    private val taskDefinitionResolver: TaskDefinitionResolver,
    private val flowStatusEventPublisher: FlowStatusEventPublisher,
) {

    fun updateTaskStatus(taskRunId: UUID, newStatus: FlowStatus): TaskRunEntity {
        return updateTaskStatus(taskRuntimeService.getTaskRun(taskRunId), newStatus)
    }

    fun updateTaskStatus(taskRun: TaskRunEntity, newStatus: FlowStatus): TaskRunEntity {
        if (taskRun.status == newStatus) {
            return taskRun
        }
        val updatedTaskRun = taskRuntimeService.setTaskRunStatus(taskRun, newStatus)
        flowStatusEventPublisher.publishTaskStatusChanged(
            TaskStatusChangedEvent(
                taskRunId = updatedTaskRun.requireId(),
                stageRunId = updatedTaskRun.stageRunId,
                oldStatus = taskRun.status,
                newStatus = updatedTaskRun.status,
            )
        )
        return updatedTaskRun
    }

    fun recomputeTaskStatus(taskRunId: UUID): TaskRunEntity {
        val taskRun = taskRuntimeService.getTaskRun(taskRunId)
        return updateTaskStatus(taskRun, resolveTaskStatus(taskRun))
    }

    private fun resolveTaskStatus(taskRun: TaskRunEntity): FlowStatus {
        return when (taskRun.status) {
            FlowStatus.PENDING -> if (taskCanBecomeReady(taskRun)) FlowStatus.READY else FlowStatus.PENDING
            FlowStatus.READY -> resolveTaskExecutionStatus(taskRun)
            else -> taskRun.status
        }
    }

    private fun taskCanBecomeReady(taskRun: TaskRunEntity): Boolean {
        val stageRun = stageRuntimeService.getStageRun(taskRun.stageRunId)
        return stageRun.status == FlowStatus.RUNNING &&
            taskRuntimeService.allTaskDependenciesSatisfied(taskRun.requireId())
    }

    private fun resolveTaskExecutionStatus(taskRun: TaskRunEntity): FlowStatus {
        return if (taskDefinitionResolver.getTaskDefinition(taskRun).waitingPolicy.waitingTask) {
            FlowStatus.WAITING
        } else {
            FlowStatus.RUNNING
        }
    }
}
