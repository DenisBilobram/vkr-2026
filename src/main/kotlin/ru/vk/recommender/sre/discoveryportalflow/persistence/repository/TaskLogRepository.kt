package ru.vk.recommender.sre.discoveryportalflow.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskLogEntity
import java.util.UUID

interface TaskLogRepository : CrudRepository<TaskLogEntity, UUID> {
    fun findByTaskRunIdOrderByCreatedAt(taskRunId: UUID): List<TaskLogEntity>
}
