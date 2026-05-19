package ru.vk.recommender.sre.discoveryportalflow.persistence.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table(name = "stage_run")
data class StageRunEntity(
    @Id
    val id: UUID? = null,

    @Column("pipeline_run_id")
    val pipelineRunId: UUID? = null,

    @Column("flow_context_id")
    val flowContextId: UUID,

    @Column("stage_name")
    val stageName: String? = null,

    @Column("pipeline_name")
    val pipelineName: String? = null,

    @Column("status")
    var status: FlowStatus = FlowStatus.PENDING,

    @Column("started_at")
    var startedAt: Long? = null,

    @Column("finished_at")
    var finishedAt: Long? = null,

    @Column("summary")
    val summary: String = "{}"
) {
    fun requireId(): UUID = requireNotNull(id) { "Stage run id is required" }
    fun requirePipelineRunId(): UUID = requireNotNull(pipelineRunId) { "Stage run pipelineRunId is required" }
    fun requireStageName(): String = requireNotNull(stageName) { "Stage run stageName is required" }
    fun requirePipelineName(): String = requireNotNull(pipelineName) { "Stage run pipelineName is required" }
}
