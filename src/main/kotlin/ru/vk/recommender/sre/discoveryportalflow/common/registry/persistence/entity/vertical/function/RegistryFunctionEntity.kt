package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.vertical.function

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "function")
data class RegistryFunctionEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("vertical_uid")
    val verticalUid: UUID,

    @Column("function_name")
    val functionName: String,
) : RegistryUidEntity


