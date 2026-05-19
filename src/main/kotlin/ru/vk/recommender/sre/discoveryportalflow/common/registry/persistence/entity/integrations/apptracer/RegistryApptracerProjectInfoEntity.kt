package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.apptracer

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "apptracer_project_info")
data class RegistryApptracerProjectInfoEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("apptracer_project_name")
    val apptracerProjectName: String,

    @Column("apptracer_project_id")
    val apptracerProjectId: String?,

    @Column("project_uid")
    val projectUid: UUID?,

    @Column("vertical_uid")
    val verticalUid: UUID?,
) : RegistryUidEntity


