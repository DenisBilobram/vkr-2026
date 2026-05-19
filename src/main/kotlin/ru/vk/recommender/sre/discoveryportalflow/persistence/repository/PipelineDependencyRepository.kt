package ru.vk.recommender.sre.discoveryportalflow.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.PipelineDependencyEntity
import java.util.UUID

interface PipelineDependencyRepository : CrudRepository<PipelineDependencyEntity, UUID> {

    fun findByDependencyPipelineRunId(dependencyPipelineRunId: UUID): List<PipelineDependencyEntity>

    fun findByPipelineRunId(pipelineRunId: UUID): List<PipelineDependencyEntity>

    fun findByPipelineRunIdIn(pipelineRunIds: Collection<UUID>): List<PipelineDependencyEntity>
}
