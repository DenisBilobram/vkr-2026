package ru.vk.recommender.sre.discoveryportalflow.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.StageRunEntity
import java.util.UUID

interface StageRunRepository : CrudRepository<StageRunEntity, UUID> {

    fun findByPipelineRunId(pipelineRunId: UUID): List<StageRunEntity>

    fun findByStatus(status: FlowStatus): List<StageRunEntity>
}
