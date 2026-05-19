package ru.vk.recommender.sre.discoveryportalflow.persistence.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.PipelineDependencyEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.PipelineRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.repository.PipelineDependencyRepository
import ru.vk.recommender.sre.discoveryportalflow.persistence.repository.PipelineRunRepository
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.PipelineChildrenType
import java.time.Instant
import java.util.UUID

@Service
class PipelineRuntimeService(
    private val pipelineRunRepository: PipelineRunRepository,
    private val pipelineDependencyRepository: PipelineDependencyRepository,
) {

    fun createPipelineRun(
        pipelineName: String,
        childrenType: PipelineChildrenType,
        flowContextId: UUID,
        parentPipelineRunId: UUID? = null,
    ): PipelineRunEntity {
        return pipelineRunRepository.save(
            PipelineRunEntity(
                flowContextId = flowContextId,
                pipelineName = pipelineName,
                childrenType = childrenType,
                parentPipelineRunId = parentPipelineRunId,
                createdAt = currentUnixSeconds(),
            )
        )
    }

    fun getPipelineRun(pipelineRunId: UUID): PipelineRunEntity {
        return pipelineRunRepository.findByIdOrNull(pipelineRunId)
            ?: error("Can't find pipeline run with id=$pipelineRunId")
    }

    fun getChildPipelineRuns(parentPipelineRunId: UUID): List<PipelineRunEntity> {
        return pipelineRunRepository.findByParentPipelineRunId(parentPipelineRunId).sortedBy { pipelineRun ->
            pipelineRun.id
        }
    }

    fun getRootPipelineRunsPage(limit: Int, offset: Long): List<PipelineRunEntity> {
        return pipelineRunRepository.findRootPipelineRunsPage(limit, offset)
    }

    fun countRootPipelineRuns(): Long {
        return pipelineRunRepository.countRootPipelineRuns()
    }

    fun getAllPipelineRuns(): List<PipelineRunEntity> = pipelineRunRepository.findAll().toList()

    fun savePipelineRunDependencies(dependencies: List<PipelineDependencyEntity>) {
        if (dependencies.isNotEmpty()) {
            pipelineDependencyRepository.saveAll(dependencies)
        }
    }

    fun getDependents(dependencyPipelineRunId: UUID): List<PipelineDependencyEntity> {
        return pipelineDependencyRepository.findByDependencyPipelineRunId(dependencyPipelineRunId)
    }

    fun getDependencies(pipelineRunId: UUID): List<PipelineDependencyEntity> {
        return pipelineDependencyRepository.findByPipelineRunId(pipelineRunId)
    }

    fun getDependenciesForPipelineRuns(pipelineRunIds: Collection<UUID>): List<PipelineDependencyEntity> {
        if (pipelineRunIds.isEmpty()) return emptyList()
        return pipelineDependencyRepository.findByPipelineRunIdIn(pipelineRunIds)
    }

    fun allPipelineDependenciesSatisfied(pipelineRunId: UUID): Boolean {
        val dependencies = getDependencies(pipelineRunId)
        if (dependencies.isEmpty()) {
            return true
        }
        val dependencyIds = dependencies.map { dependency -> dependency.dependencyPipelineRunId }.distinct()
        val dependencyRuns = pipelineRunRepository.findAllById(dependencyIds).associateBy { pipelineRun -> pipelineRun.requireId() }
        dependencyIds.forEach { dependencyId ->
            requireNotNull(dependencyRuns[dependencyId]) { "No pipeline run found for dependency id=$dependencyId" }
        }
        return dependencyRuns.values.all { dependencyRun -> dependencyRun.status in dependencySatisfiedStatuses }
    }

    fun setPipelineRunStatus(pipelineRunEntity: PipelineRunEntity, status: FlowStatus): PipelineRunEntity {
        val startedAt = if (status == FlowStatus.RUNNING) {
            pipelineRunEntity.startedAt ?: currentUnixSeconds()
        } else {
            pipelineRunEntity.startedAt
        }
        val finishedAt = if (status in terminalStatuses) currentUnixSeconds() else null

        val updated = pipelineRunEntity.copy(
            status = status,
            startedAt = startedAt,
            finishedAt = finishedAt,
        )
        return pipelineRunRepository.save(updated)
    }

    val terminalStatuses = setOf(
        FlowStatus.SUCCEEDED,
        FlowStatus.FAILED,
        FlowStatus.CANCELED,
        FlowStatus.SKIPPED,
    )

    val dependencySatisfiedStatuses = setOf(
        FlowStatus.SUCCEEDED,
        FlowStatus.SKIPPED,
    )

    private fun currentUnixSeconds(): Long = Instant.now().epochSecond
}
