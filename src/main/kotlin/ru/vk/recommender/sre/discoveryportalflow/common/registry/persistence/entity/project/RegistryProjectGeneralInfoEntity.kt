package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.project

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "project_general_info")
data class RegistryProjectGeneralInfoEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("project_uid")
    val projectUid: UUID,

    @Column("display_name")
    val displayName: String? = null,

    @Column("product_id")
    val productId: Int?,

    @Column("prm")
    val prm: String?,

    @Column("toggles_offline_tenant")
    val togglesOfflineTenant: String?,

    @Column("links")
    val links: String,
) : RegistryUidEntity
