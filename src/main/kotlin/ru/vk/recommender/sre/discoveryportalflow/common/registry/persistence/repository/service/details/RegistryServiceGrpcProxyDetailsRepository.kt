package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceGrpcProxyDetailsEntity

interface RegistryServiceGrpcProxyDetailsRepository : CrudRepository<RegistryServiceGrpcProxyDetailsEntity, UUID> {
    fun findByServiceUid(serviceUid: UUID): RegistryServiceGrpcProxyDetailsEntity?
}
