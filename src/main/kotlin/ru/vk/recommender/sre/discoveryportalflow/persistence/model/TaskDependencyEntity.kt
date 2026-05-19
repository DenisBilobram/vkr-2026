package ru.vk.recommender.sre.discoveryportalflow.persistence.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("task_dependency")
data class TaskDependencyEntity(
    @Id
    val id: UUID? = null,

    @Column("task_run_id")
    val taskRunId: UUID,

    @Column("dependency_task_run_id")
    val dependencyTaskRunId: UUID,
) {
    fun requireId(): UUID = requireNotNull(id) { "Task dependency id is required" }
}
