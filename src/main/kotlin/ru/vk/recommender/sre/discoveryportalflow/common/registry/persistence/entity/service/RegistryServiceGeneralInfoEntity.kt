package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "service_general_info")
data class RegistryServiceGeneralInfoEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("service_uid")
    val serviceUid: UUID,

    @Column("pms_application")
    val pmsApplication: String?,

    @Column("hermes_group")
    val hermesGroup: String?,

    @Column("links")
    val links: String,
) : RegistryUidEntity
