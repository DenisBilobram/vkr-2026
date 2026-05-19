package ru.vk.recommender.sre.discoveryportalflow.persistence.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("pipeline_dependency")
data class PipelineDependencyEntity(
    @Id
    val id: UUID? = null,

    @Column("pipeline_run_id")
    val pipelineRunId: UUID,

    @Column("dependency_pipeline_run_id")
    val dependencyPipelineRunId: UUID,
) {
    fun requireId(): UUID = requireNotNull(id) { "Pipeline dependency id is required" }
}
