package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.vertical

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.vertical.RegistryVerticalEntity

interface RegistryVerticalRepository : CrudRepository<RegistryVerticalEntity, UUID> {
    fun findByProjectUidAndVerticalName(projectUid: UUID, verticalName: String): RegistryVerticalEntity?
    fun findAllByProjectUid(projectUid: UUID): List<RegistryVerticalEntity>
}
