package ru.vk.recommender.sre.discoveryportalflow.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.StageDependencyEntity
import java.util.UUID

interface StageDependencyRepository : CrudRepository<StageDependencyEntity, UUID> {

    fun findByDependencyStageRunId(dependencyStageRunId: UUID): List<StageDependencyEntity>

    fun findByStageRunId(stageRunId: UUID): List<StageDependencyEntity>

    fun findByStageRunIdIn(stageRunIds: Collection<UUID>): List<StageDependencyEntity>
}
