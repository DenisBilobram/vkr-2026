package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType

@Table(schema = "registry", name = "service")
data class RegistryServiceEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("cloud_service")
    val cloudService: String,

    @Column("service_type")
    val serviceType: ServiceType,

    @Column("project_uid")
    val projectUid: UUID?,

    @Column("vertical_uid")
    val verticalUid: UUID?,
) : RegistryUidEntity

