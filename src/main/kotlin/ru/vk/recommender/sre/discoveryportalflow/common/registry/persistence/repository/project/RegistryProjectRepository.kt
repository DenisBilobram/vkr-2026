package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.project

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.project.RegistryProjectEntity

interface RegistryProjectRepository : CrudRepository<RegistryProjectEntity, UUID> {
    fun findByProjectName(projectName: String): RegistryProjectEntity?
}
