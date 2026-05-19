package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.vertical

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "vertical")
data class RegistryVerticalEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("project_uid")
    val projectUid: UUID,

    @Column("vertical_name")
    val verticalName: String,

    @Column("release_status")
    val releaseStatus: String,
) : RegistryUidEntity

