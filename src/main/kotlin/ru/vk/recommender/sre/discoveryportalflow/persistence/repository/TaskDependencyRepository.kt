package ru.vk.recommender.sre.discoveryportalflow.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskDependencyEntity
import java.util.UUID

interface TaskDependencyRepository : CrudRepository<TaskDependencyEntity, UUID> {

    fun findByDependencyTaskRunId(dependencyTaskRunId: UUID): List<TaskDependencyEntity>

    fun findByTaskRunId(taskRunId: UUID): List<TaskDependencyEntity>

    fun findByTaskRunIdIn(taskRunIds: Collection<UUID>): List<TaskDependencyEntity>
}
