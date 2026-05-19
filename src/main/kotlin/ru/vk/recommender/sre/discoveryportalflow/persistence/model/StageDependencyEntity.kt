package ru.vk.recommender.sre.discoveryportalflow.persistence.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("stage_dependency")
data class StageDependencyEntity(
    @Id
    val id: UUID? = null,

    @Column("stage_run_id")
    val stageRunId: UUID,

    @Column("dependency_stage_run_id")
    val dependencyStageRunId: UUID,
) {
    fun requireId(): UUID = requireNotNull(id) { "Stage dependency id is required" }
}
