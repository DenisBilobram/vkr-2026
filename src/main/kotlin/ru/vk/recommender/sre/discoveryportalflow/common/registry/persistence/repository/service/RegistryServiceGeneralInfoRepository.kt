package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.RegistryServiceGeneralInfoEntity

interface RegistryServiceGeneralInfoRepository : CrudRepository<RegistryServiceGeneralInfoEntity, UUID> {
    fun findByServiceUid(serviceUid: UUID): RegistryServiceGeneralInfoEntity?
}
