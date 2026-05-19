package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "service_snapshots_builder_details")
data class RegistryServiceSnapshotsBuilderDetailsEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("service_uid")
    val serviceUid: UUID,
) : RegistryUidEntity


