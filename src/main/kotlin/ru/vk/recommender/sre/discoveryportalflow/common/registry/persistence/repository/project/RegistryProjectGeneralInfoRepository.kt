package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.project

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.project.RegistryProjectGeneralInfoEntity

interface RegistryProjectGeneralInfoRepository : CrudRepository<RegistryProjectGeneralInfoEntity, UUID> {
    fun findByProjectUid(projectUid: UUID): RegistryProjectGeneralInfoEntity?
}
