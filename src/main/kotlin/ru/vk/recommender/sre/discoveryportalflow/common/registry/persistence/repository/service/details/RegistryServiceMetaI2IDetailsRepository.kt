package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceMetaI2IDetailsEntity

interface RegistryServiceMetaI2IDetailsRepository : CrudRepository<RegistryServiceMetaI2IDetailsEntity, UUID> {
    fun findByServiceUid(serviceUid: UUID): RegistryServiceMetaI2IDetailsEntity?
}
