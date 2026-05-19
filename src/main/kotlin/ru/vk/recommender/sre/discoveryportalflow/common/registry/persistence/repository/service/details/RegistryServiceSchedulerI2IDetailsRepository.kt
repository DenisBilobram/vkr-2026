package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceSchedulerI2IDetailsEntity

interface RegistryServiceSchedulerI2IDetailsRepository : CrudRepository<RegistryServiceSchedulerI2IDetailsEntity, UUID> {
    fun findByServiceUid(serviceUid: UUID): RegistryServiceSchedulerI2IDetailsEntity?
}
