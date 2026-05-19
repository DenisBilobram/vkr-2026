package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.project

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "project")
data class RegistryProjectEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("project_name")
    val projectName: String,
) : RegistryUidEntity

