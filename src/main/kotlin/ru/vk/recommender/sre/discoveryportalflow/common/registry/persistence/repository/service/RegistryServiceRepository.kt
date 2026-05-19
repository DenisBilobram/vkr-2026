package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.RegistryServiceEntity
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType

interface RegistryServiceRepository : CrudRepository<RegistryServiceEntity, UUID> {
    fun findByProjectUidAndServiceType(projectUid: UUID, serviceType: ServiceType): RegistryServiceEntity?
    fun findByVerticalUidAndServiceType(verticalUid: UUID, serviceType: ServiceType): RegistryServiceEntity?
    fun findByCloudService(cloudService: String): RegistryServiceEntity?
    fun findAllByProjectUid(projectUid: UUID): List<RegistryServiceEntity>
    fun findAllByVerticalUid(verticalUid: UUID): List<RegistryServiceEntity>
}
