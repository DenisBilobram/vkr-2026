package ru.vk.recommender.sre.discoveryportalflow.persistence.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("task_run")
data class TaskRunEntity(
    @Id
    val id: UUID? = null,

    @Column("task_name")
    val taskName: String,

    @Column("status")
    var status: FlowStatus = FlowStatus.PENDING,

    @Column("started_at")
    val startedAt: Long? = null,

    @Column("finished_at")
    val finishedAt: Long? = null,

    @Column("attempt_number")
    val attemptNumber: Int = 0,

    @Column("stage_run_id")
    val stageRunId: UUID,
) {
    fun requireId(): UUID = requireNotNull(id) { "Task run id is required" }
}
