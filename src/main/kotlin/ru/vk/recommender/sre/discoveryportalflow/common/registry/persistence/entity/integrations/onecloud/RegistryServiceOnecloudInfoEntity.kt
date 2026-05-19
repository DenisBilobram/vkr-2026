package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "service_onecloud_info")
data class RegistryServiceOnecloudInfoEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("service_uid")
    val serviceUid: UUID,

    @Column("cloud_service_id")
    val cloudServiceId: String?,

    @Column("production_queue")
    val productionQueue: String?,

    @Column("canary_queue")
    val canaryQueue: String?,

    @Column("testing_queue")
    val testingQueue: String?,

    @Column("subqueues")
    val subqueues: Array<String>?,
) : RegistryUidEntity
