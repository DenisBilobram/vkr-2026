package ru.vk.recommender.sre.discoveryportalflow.persistence.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.PipelineRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.StageDependencyEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.StageRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.repository.StageDependencyRepository
import ru.vk.recommender.sre.discoveryportalflow.persistence.repository.StageRunRepository
import java.time.Instant
import java.util.UUID

@Service
class StageRuntimeService(
    private val stageRunRepository: StageRunRepository,
    private val stageDependencyRepository: StageDependencyRepository,
) {

    fun createPipelineStageRun(
        pipelineRun: PipelineRunEntity,
        stageName: String,
    ): StageRunEntity {
        return stageRunRepository.save(
            StageRunEntity(
                pipelineRunId = pipelineRun.requireId(),
                flowContextId = pipelineRun.flowContextId,
                stageName = stageName,
                pipelineName = pipelineRun.pipelineName,
            )
        )
    }

    fun getStageRun(stageRunId: UUID): StageRunEntity {
        return stageRunRepository.findByIdOrNull(stageRunId)
            ?: error("Can't find stage run with id=$stageRunId")
    }

    fun getStageRunsForPipelineRun(pipelineRunId: UUID): List<StageRunEntity> {
        return stageRunRepository.findByPipelineRunId(pipelineRunId).sortedBy { stageRun ->
            stageRun.id
        }
    }

    fun getAllStageRuns(): List<StageRunEntity> = stageRunRepository.findAll().toList()

    fun setStageRunStatus(stageRunEntity: StageRunEntity, status: FlowStatus): StageRunEntity {
        val startedAt = if (status == FlowStatus.RUNNING) {
            stageRunEntity.startedAt ?: currentUnixSeconds()
        } else {
            stageRunEntity.startedAt
        }
        val finishedAt = if (status in terminalStatuses) currentUnixSeconds() else null

        val updated = stageRunEntity.copy(
            status = status,
            startedAt = startedAt,
            finishedAt = finishedAt,
        )
        return stageRunRepository.save(updated)
    }

    fun saveStageRunDependencies(dependencies: List<StageDependencyEntity>) {
        if (dependencies.isNotEmpty()) {
            stageDependencyRepository.saveAll(dependencies)
        }
    }

    fun getDependents(dependencyStageRunId: UUID): List<StageDependencyEntity> {
        return stageDependencyRepository.findByDependencyStageRunId(dependencyStageRunId)
    }

    fun getDependencies(stageRunId: UUID): List<StageDependencyEntity> {
        return stageDependencyRepository.findByStageRunId(stageRunId)
    }

    fun getDependenciesForStageRuns(stageRunIds: Collection<UUID>): List<StageDependencyEntity> {
        if (stageRunIds.isEmpty()) return emptyList()
        return stageDependencyRepository.findByStageRunIdIn(stageRunIds)
    }

    fun allStageDependenciesSatisfied(stageRunId: UUID): Boolean {
        val dependencies = getDependencies(stageRunId)
        if (dependencies.isEmpty()) {
            return true
        }
        val dependencyIds = dependencies.map { dependency -> dependency.dependencyStageRunId }.distinct()
        val dependencyRuns = stageRunRepository.findAllById(dependencyIds).associateBy { stageRun -> stageRun.requireId() }
        dependencyIds.forEach { dependencyId ->
            requireNotNull(dependencyRuns[dependencyId]) { "No stage run found for dependency id=$dependencyId" }
        }
        return dependencyRuns.values.all { dependencyRun -> dependencyRun.status in dependencySatisfiedStatuses }
    }

    val dependencySatisfiedStatuses = setOf(
        FlowStatus.SUCCEEDED,
        FlowStatus.SKIPPED,
    )

    val terminalStatuses = setOf(
        FlowStatus.SUCCEEDED,
        FlowStatus.FAILED,
        FlowStatus.CANCELED,
        FlowStatus.SKIPPED,
    )

    private fun currentUnixSeconds(): Long = Instant.now().epochSecond
}
