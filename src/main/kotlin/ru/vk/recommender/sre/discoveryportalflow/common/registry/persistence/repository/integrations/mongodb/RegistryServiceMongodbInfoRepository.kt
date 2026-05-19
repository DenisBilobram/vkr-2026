package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.integrations.mongodb

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.mongodb.RegistryServiceMongodbInfoEntity

interface RegistryServiceMongodbInfoRepository : CrudRepository<RegistryServiceMongodbInfoEntity, UUID> {
    fun findByServiceUid(serviceUid: UUID): RegistryServiceMongodbInfoEntity?
}
