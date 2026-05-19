package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence

import java.util.UUID

interface RegistryUidEntity {
    var uid: UUID?

    fun requireId(): UUID = requireNotNull(uid) { "uid is required" }
}

