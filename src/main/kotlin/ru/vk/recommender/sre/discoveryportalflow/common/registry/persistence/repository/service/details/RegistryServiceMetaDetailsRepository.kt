package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceMetaDetailsEntity

interface RegistryServiceMetaDetailsRepository : CrudRepository<RegistryServiceMetaDetailsEntity, UUID> {
    fun findByServiceUid(serviceUid: UUID): RegistryServiceMetaDetailsEntity?
}
