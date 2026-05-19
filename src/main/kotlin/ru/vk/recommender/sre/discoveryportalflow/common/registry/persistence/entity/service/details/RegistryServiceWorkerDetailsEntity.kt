package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "service_worker_details")
data class RegistryServiceWorkerDetailsEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("service_uid")
    val serviceUid: UUID,

    @Column("default_pool")
    val defaultPool: String?,
) : RegistryUidEntity


