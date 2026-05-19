package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.integrations.onecloud

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.RegistryProjectOnecloudInfoEntity

interface RegistryProjectOnecloudInfoRepository : CrudRepository<RegistryProjectOnecloudInfoEntity, UUID> {
    fun findByProjectUid(projectUid: UUID): RegistryProjectOnecloudInfoEntity?
}
