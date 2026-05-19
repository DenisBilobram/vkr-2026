package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.integrations.teamcity

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.teamcity.RegistryTeamcityProjectEntity

interface RegistryTeamcityProjectRepository : CrudRepository<RegistryTeamcityProjectEntity, UUID> {
    fun findByProjectUid(projectUid: UUID): RegistryTeamcityProjectEntity?

    fun findByVerticalUid(verticalUid: UUID): RegistryTeamcityProjectEntity?

    fun findByServiceUid(serviceUid: UUID): RegistryTeamcityProjectEntity?
}
