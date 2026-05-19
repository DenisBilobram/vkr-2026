package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service

import java.util.UUID

data class RegistryServiceScope(
    val projectUid: UUID? = null,
    val verticalUid: UUID? = null,
) {
    init {
        require((projectUid == null) xor (verticalUid == null)) {
            "Registry service scope must reference either project or vertical"
        }
    }

    companion object {
        fun project(projectUid: UUID): RegistryServiceScope = RegistryServiceScope(projectUid = projectUid)

        fun vertical(verticalUid: UUID): RegistryServiceScope = RegistryServiceScope(verticalUid = verticalUid)
    }
}
