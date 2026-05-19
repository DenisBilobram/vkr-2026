package ru.vk.recommender.sre.discoveryportalflow.persistence.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.StageRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskDependencyEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.repository.TaskDependencyRepository
import ru.vk.recommender.sre.discoveryportalflow.persistence.repository.TaskRunRepository
import ru.vk.recommender.sre.discoveryportalflow.service.engine.logging.TasksRuntimeLogger
import java.time.Instant
import java.util.UUID

@Service
class TaskRuntimeService(
    private val taskRunRepository: TaskRunRepository,
    private val taskDependencyRepository: TaskDependencyRepository,
    private val tasksRuntimeLogger: TasksRuntimeLogger,
) {

    fun createTaskRun(stageRun: StageRunEntity, taskName: String): TaskRunEntity {
        return taskRunRepository.save(
            TaskRunEntity(
                taskName = taskName,
                stageRunId = stageRun.requireId(),
            )
        )
    }

    fun setTaskRunStatus(taskRun: TaskRunEntity, status: FlowStatus): TaskRunEntity {
        val startedAt = if (status == FlowStatus.RUNNING || status == FlowStatus.WAITING) {
            taskRun.startedAt ?: currentUnixSeconds()
        } else {
            taskRun.startedAt
        }
        val finishedAt = if (status in terminalStatuses) currentUnixSeconds() else null

        val updated = taskRun.copy(
            status = status,
            startedAt = startedAt,
            finishedAt = finishedAt,
        )
        val savedTaskRun = taskRunRepository.save(updated)
        tasksRuntimeLogger.logStatusChange(
            taskRunId = savedTaskRun.requireId(),
            status = status,
        )
        return savedTaskRun
    }

    fun incrementTaskRunAttempt(taskRun: TaskRunEntity): TaskRunEntity {
        return taskRunRepository.save(taskRun.copy(attemptNumber = taskRun.attemptNumber + 1))
    }

    fun saveTaskRunDependencies(dependencies: List<TaskDependencyEntity>) {
        if (dependencies.isNotEmpty()) {
            taskDependencyRepository.saveAll(dependencies)
        }
    }

    fun getDependents(dependencyTaskRunId: UUID): List<TaskDependencyEntity> {
        return taskDependencyRepository.findByDependencyTaskRunId(dependencyTaskRunId)
    }

    fun allTaskDependenciesSatisfied(taskRunId: UUID): Boolean {
        val dependencies = taskDependencyRepository.findByTaskRunId(taskRunId)
        if (dependencies.isEmpty()) {
            return true
        }
        val dependencyIds = dependencies.map { dependency -> dependency.dependencyTaskRunId }.distinct()
        val dependencyRuns = taskRunRepository.findAllById(dependencyIds)
            .associateBy { taskRun -> taskRun.requireId() }
        dependencyIds.forEach { dependencyId ->
            requireNotNull(dependencyRuns[dependencyId]) { "No task run found for dependency id=$dependencyId" }
        }
        return dependencyRuns.values.all { dependencyRun -> dependencyRun.status in dependencySatisfiedStatuses }
    }

    fun getTaskRun(taskRunId: UUID): TaskRunEntity {
        return taskRunRepository.findByIdOrNull(taskRunId)
            ?: error("No task run found for id=$taskRunId")
    }

    fun getTaskRunsForStageRun(stageRun: StageRunEntity): List<TaskRunEntity> {
        return getTaskRunsForStageRunId(stageRun.requireId())
    }

    fun getTaskRunsForStageRunId(stageRunId: UUID): List<TaskRunEntity> {
        return taskRunRepository.findTaskRunEntitiesByStageRunId(stageRunId).sortedBy { taskRun ->
            taskRun.id
        }
    }

    fun getTaskRunsForStageRunIds(stageRunIds: Collection<UUID>): List<TaskRunEntity> {
        if (stageRunIds.isEmpty()) return emptyList()
        return taskRunRepository.findByStageRunIdIn(stageRunIds).sortedWith(
            compareBy<TaskRunEntity> { taskRun -> taskRun.stageRunId }
                .thenBy { taskRun -> taskRun.id },
        )
    }

    fun getTaskRunsByStatus(status: FlowStatus): List<TaskRunEntity> {
        return taskRunRepository.findByStatus(status).sortedBy { taskRun -> taskRun.id }
    }

    fun getTaskRunsByStatuses(statuses: Collection<FlowStatus>): List<TaskRunEntity> {
        if (statuses.isEmpty()) return emptyList()
        return taskRunRepository.findByStatusIn(statuses).sortedBy { taskRun -> taskRun.id }
    }

    val dependencySatisfiedStatuses = setOf(
        FlowStatus.SUCCEEDED,
        FlowStatus.SKIPPED,
    )

    val terminalStatuses = setOf(
        FlowStatus.SUCCEEDED,
        FlowStatus.SKIPPED,
        FlowStatus.FAILED,
        FlowStatus.CANCELED,
    )

    val recoverableLaunchStatuses = setOf(
        FlowStatus.PENDING,
        FlowStatus.READY,
    )

    private fun currentUnixSeconds(): Long = Instant.now().epochSecond
}
