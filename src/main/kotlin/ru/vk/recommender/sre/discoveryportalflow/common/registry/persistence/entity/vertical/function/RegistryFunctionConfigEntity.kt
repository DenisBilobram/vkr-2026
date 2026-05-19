package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.vertical.function

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType

@Table(schema = "registry", name = "function_config")
data class RegistryFunctionConfigEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("function_uid")
    val functionUid: UUID,

    @Column("service_type")
    val serviceType: ServiceType,

    @Column("config")
    val config: String,
) : RegistryUidEntity


