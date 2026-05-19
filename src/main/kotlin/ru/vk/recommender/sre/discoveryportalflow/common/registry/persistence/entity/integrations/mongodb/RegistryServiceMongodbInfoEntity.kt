package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.mongodb

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "service_mongodb_info")
data class RegistryServiceMongodbInfoEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("service_uid")
    val serviceUid: UUID,

    @Column("database_name")
    val databaseName: String?,

    @Column("user_name")
    val userName: String?,
) : RegistryUidEntity

