package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.onecloud

import java.util.UUID
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.DatacenterCode
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.RegistryOnecloudDatacenterEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.integrations.onecloud.RegistryOnecloudDatacenterRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.toRegistryDatacenterCodes
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

@Service
class RegistryOnecloudDatacenterPersistenceService(
    private val onecloudDatacenterRepository: RegistryOnecloudDatacenterRepository,
) {

    fun syncVerticalDatacenters(
        verticalUid: UUID,
        dcSettings: RecommenderDcSettings,
    ) {
        val datacenters = dcSettings.toDatacenterRows(
            projectUid = null,
            verticalUid = verticalUid,
            serviceUid = null,
            supportsCanary = true,
            supportsTesting = true,
        )
        replaceDatacenters(
            existing = onecloudDatacenterRepository.findAllByVerticalUid(verticalUid),
            replacement = datacenters,
        )
    }

    fun syncProjectDatacenters(
        projectUid: UUID,
        dcSettings: RecommenderDcSettings,
    ) {
        val datacenters = dcSettings.toDatacenterRows(
            projectUid = projectUid,
            verticalUid = null,
            serviceUid = null,
            supportsCanary = true,
            supportsTesting = true,
        )
        replaceDatacenters(
            existing = onecloudDatacenterRepository.findAllByProjectUid(projectUid),
            replacement = datacenters,
        )
    }

    fun syncServiceDatacenters(
        serviceUid: UUID,
        serviceRuntime: ServiceRuntime,
        dcSettings: RecommenderDcSettings,
    ) {
        val datacenters = dcSettings.toDatacenterRows(
            projectUid = null,
            verticalUid = null,
            serviceUid = serviceUid,
            supportsCanary = serviceRuntime.supports(ServiceEnvironment.CANARY),
            supportsTesting = serviceRuntime.supports(ServiceEnvironment.TESTING),
        )
        replaceDatacenters(
            existing = onecloudDatacenterRepository.findAllByServiceUid(serviceUid),
            replacement = datacenters,
        )
    }

    private fun replaceDatacenters(
        existing: List<RegistryOnecloudDatacenterEntity>,
        replacement: List<RegistryOnecloudDatacenterEntity>,
    ) {
        if (existing.isNotEmpty()) {
            onecloudDatacenterRepository.deleteAll(existing)
        }
        if (replacement.isNotEmpty()) {
            onecloudDatacenterRepository.saveAll(replacement)
        }
    }

    private fun RecommenderDcSettings.toDatacenterRows(
        projectUid: UUID?,
        verticalUid: UUID?,
        serviceUid: UUID?,
        supportsCanary: Boolean,
        supportsTesting: Boolean,
    ): List<RegistryOnecloudDatacenterEntity> {
        val productionDatacenters = productionDcs.toRegistryDatacenterCodes()
        val canaryDatacenters = canaryDcs.toRegistryDatacenterCodes().toSet()
        val testingDatacenters = testingDcs.toRegistryDatacenterCodes().toSet()

        require(canaryDatacenters.all(productionDatacenters::contains)) {
            "Canary datacenters must be a subset of production datacenters"
        }
        require(testingDatacenters.all(productionDatacenters::contains)) {
            "Testing datacenters must be a subset of production datacenters"
        }

        return productionDatacenters.map { datacenterCode ->
            RegistryOnecloudDatacenterEntity(
                uid = null,
                projectUid = projectUid,
                verticalUid = verticalUid,
                serviceUid = serviceUid,
                datacenterCode = datacenterCode,
                isCanary = supportsCanary && datacenterCode in canaryDatacenters,
                isTesting = supportsTesting && datacenterCode in testingDatacenters,
            )
        }
    }
}
