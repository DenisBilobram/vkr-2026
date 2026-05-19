package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.vertical.function

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.vertical.function.RegistryFunctionEntity

interface RegistryFunctionRepository : CrudRepository<RegistryFunctionEntity, UUID> {
    fun findByVerticalUidAndFunctionName(verticalUid: UUID, functionName: String): RegistryFunctionEntity?
    fun findAllByVerticalUid(verticalUid: UUID): List<RegistryFunctionEntity>
}
