package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onesecret

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "onesecret_secret")
data class RegistryOnesecretSecretEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("project_uid")
    val projectUid: UUID?,

    @Column("vertical_uid")
    val verticalUid: UUID?,

    @Column("service_uid")
    val serviceUid: UUID?,

    @Column("secret_id")
    val secretId: String?,

    @Column("testing_secret_id")
    val testingSecretId: String?,
) : RegistryUidEntity


