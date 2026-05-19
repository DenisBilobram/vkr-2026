package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "vertical_onecloud_info")
data class RegistryVerticalOnecloudInfoEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("vertical_uid")
    val verticalUid: UUID,

    @Column("production_root_queue")
    val productionRootQueue: String?,

    @Column("testing_root_queue")
    val testingRootQueue: String?,

    @Column("i2i_production_root_queue")
    val i2iProductionRootQueue: String?,
) : RegistryUidEntity

