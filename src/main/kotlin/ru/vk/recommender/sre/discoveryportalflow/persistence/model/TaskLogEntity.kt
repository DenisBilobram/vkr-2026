package ru.vk.recommender.sre.discoveryportalflow.persistence.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("task_log")
data class TaskLogEntity(
    @Id
    val id: UUID? = null,

    @Column("task_run_id")
    val taskRunId: UUID,

    @Column("status")
    val status: FlowStatus? = null,

    @Column("type")
    val type: LogType,

    @Column("message")
    val message: String,

    @Column("created_at")
    val createdAt: Long = Instant.now().epochSecond,
) {
    fun requireId(): UUID = requireNotNull(id) { "Task log id is required" }
}
