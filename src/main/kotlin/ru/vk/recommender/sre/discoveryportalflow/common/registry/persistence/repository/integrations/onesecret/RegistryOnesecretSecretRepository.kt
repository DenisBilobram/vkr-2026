package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.integrations.onesecret

import java.util.UUID
import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onesecret.RegistryOnesecretSecretEntity

interface RegistryOnesecretSecretRepository : CrudRepository<RegistryOnesecretSecretEntity, UUID> {
    fun findByProjectUid(projectUid: UUID): RegistryOnesecretSecretEntity?
    fun findByVerticalUid(verticalUid: UUID): RegistryOnesecretSecretEntity?
    fun findByServiceUid(serviceUid: UUID): RegistryOnesecretSecretEntity?
}
