package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.integrations.hermes

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.hermes.RegistryServiceSnapshotInfoEntity

interface RegistryServiceSnapshotInfoRepository : CrudRepository<RegistryServiceSnapshotInfoEntity, UUID> {
    fun findAllByServiceUid(serviceUid: UUID): List<RegistryServiceSnapshotInfoEntity>
}
