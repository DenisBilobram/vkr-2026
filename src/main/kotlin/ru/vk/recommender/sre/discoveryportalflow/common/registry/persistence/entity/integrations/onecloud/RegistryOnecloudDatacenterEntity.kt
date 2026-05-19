package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "onecloud_datacenter")
data class RegistryOnecloudDatacenterEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("project_uid")
    val projectUid: UUID?,

    @Column("vertical_uid")
    val verticalUid: UUID?,

    @Column("service_uid")
    val serviceUid: UUID?,

    @Column("dc")
    val datacenterCode: DatacenterCode?,

    @Column("is_canary")
    val isCanary: Boolean?,

    @Column("is_testing")
    val isTesting: Boolean?,
) : RegistryUidEntity
