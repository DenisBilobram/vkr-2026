package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceSelectorsDetailsEntity

interface RegistryServiceSelectorsDetailsRepository : CrudRepository<RegistryServiceSelectorsDetailsEntity, UUID> {
    fun findByServiceUid(serviceUid: UUID): RegistryServiceSelectorsDetailsEntity?
}
