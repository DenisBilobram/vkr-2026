package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.vertical

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.vertical.RegistryVerticalGeneralInfoEntity

interface RegistryVerticalGeneralInfoRepository : CrudRepository<RegistryVerticalGeneralInfoEntity, UUID> {
    fun findByVerticalUid(verticalUid: UUID): RegistryVerticalGeneralInfoEntity?
}
