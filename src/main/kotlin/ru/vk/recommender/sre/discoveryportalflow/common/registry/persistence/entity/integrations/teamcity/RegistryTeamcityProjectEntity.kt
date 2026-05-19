package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.teamcity

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "teamcity_project")
data class RegistryTeamcityProjectEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("project_uid")
    val projectUid: UUID?,

    @Column("vertical_uid")
    val verticalUid: UUID?,

    @Column("service_uid")
    val serviceUid: UUID?,

    @Column("teamcity_project_id")
    val teamcityProjectId: String?,
) : RegistryUidEntity
