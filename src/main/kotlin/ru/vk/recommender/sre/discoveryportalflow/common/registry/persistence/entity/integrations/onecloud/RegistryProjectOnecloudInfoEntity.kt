package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "project_onecloud_info")
data class RegistryProjectOnecloudInfoEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("project_uid")
    val projectUid: UUID,

    @Column("cloud_namespace")
    val cloudNamespace: String?,

    @Column("production_root_queue")
    val productionRootQueue: String?,

    @Column("testing_root_queue")
    val testingRootQueue: String?,
) : RegistryUidEntity

