package ru.vk.recommender.sre.discoveryportalflow.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskRunEntity
import java.util.UUID

interface TaskRunRepository : CrudRepository<TaskRunEntity, UUID> {

    fun findTaskRunEntitiesByStageRunId(id: UUID): List<TaskRunEntity>

    fun findByStageRunIdIn(stageRunIds: Collection<UUID>): List<TaskRunEntity>

    fun findByStatus(statuses: FlowStatus): List<TaskRunEntity>
    fun findByStatusIn(statuses: Collection<FlowStatus>): List<TaskRunEntity>

}
