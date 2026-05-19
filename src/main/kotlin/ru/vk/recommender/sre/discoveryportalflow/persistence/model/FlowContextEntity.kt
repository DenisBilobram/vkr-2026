package ru.vk.recommender.sre.discoveryportalflow.persistence.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table(name = "flow_context")
data class FlowContextEntity(
    @Id
    val id: UUID? = null,

    @Column("context")
    val context: String,
) {
    fun requireId(): UUID = requireNotNull(id) { "Flow context id is required" }
}
