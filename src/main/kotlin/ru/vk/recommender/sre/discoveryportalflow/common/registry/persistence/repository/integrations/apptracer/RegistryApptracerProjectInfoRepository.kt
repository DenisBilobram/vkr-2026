package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.integrations.apptracer

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.apptracer.RegistryApptracerProjectInfoEntity

interface RegistryApptracerProjectInfoRepository : CrudRepository<RegistryApptracerProjectInfoEntity, UUID> {
    fun findByProjectUid(projectUid: UUID): RegistryApptracerProjectInfoEntity?
    fun findByVerticalUid(verticalUid: UUID): RegistryApptracerProjectInfoEntity?
}
