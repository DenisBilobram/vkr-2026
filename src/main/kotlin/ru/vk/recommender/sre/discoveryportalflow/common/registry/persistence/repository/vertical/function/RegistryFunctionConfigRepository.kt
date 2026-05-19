package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.vertical.function

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.vertical.function.RegistryFunctionConfigEntity
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType

interface RegistryFunctionConfigRepository : CrudRepository<RegistryFunctionConfigEntity, UUID> {
    fun findByFunctionUidAndServiceType(functionUid: UUID, serviceType: ServiceType): RegistryFunctionConfigEntity?
    fun findAllByFunctionUid(functionUid: UUID): List<RegistryFunctionConfigEntity>
}
