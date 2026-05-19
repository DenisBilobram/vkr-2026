package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.integrations.onecloud

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.RegistryOnecloudDatacenterEntity

interface RegistryOnecloudDatacenterRepository : CrudRepository<RegistryOnecloudDatacenterEntity, UUID> {
    fun findAllByProjectUid(projectUid: UUID): List<RegistryOnecloudDatacenterEntity>

    fun findAllByVerticalUid(verticalUid: UUID): List<RegistryOnecloudDatacenterEntity>

    fun findAllByServiceUid(serviceUid: UUID): List<RegistryOnecloudDatacenterEntity>
}
