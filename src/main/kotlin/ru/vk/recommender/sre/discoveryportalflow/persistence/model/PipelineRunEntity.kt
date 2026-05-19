package ru.vk.recommender.sre.discoveryportalflow.persistence.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.PipelineChildrenType
import java.util.UUID

@Table(name = "pipeline_run")
data class PipelineRunEntity(
    @Id
    val id: UUID? = null,

    @Column("flow_context_id")
    val flowContextId: UUID,

    @Column("pipeline_name")
    val pipelineName: String,

    @Column("children_type")
    val childrenType: PipelineChildrenType,

    @Column("parent_pipeline_run_id")
    val parentPipelineRunId: UUID? = null,

    @Column("status")
    var status: FlowStatus = FlowStatus.PENDING,

    @Column("created_at")
    val createdAt: Long,

    @Column("started_at")
    var startedAt: Long? = null,

    @Column("finished_at")
    var finishedAt: Long? = null,

    @Column("summary")
    val summary: String = "{}",
) {
    fun requireId(): UUID = requireNotNull(id) { "Pipeline run id is required" }
}
