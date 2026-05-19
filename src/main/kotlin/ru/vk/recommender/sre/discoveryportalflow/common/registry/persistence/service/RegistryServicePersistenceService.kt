package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service

import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.mongodb.RegistryServiceMongodbInfoEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.RegistryServiceOnecloudInfoEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.RegistryServiceEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.RegistryServiceGeneralInfoEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.integrations.mongodb.RegistryServiceMongodbInfoRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.integrations.onecloud.RegistryServiceOnecloudInfoRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.RegistryServiceGeneralInfoRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.RegistryServiceRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.details.RegistryServiceDetailsPersistenceService
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.onecloud.RegistryOnecloudDatacenterPersistenceService
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.teamcity.RegistryTeamcityProjectPersistenceService
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.service.MdbMongoCredentialsFactory
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

@Service
class RegistryServicePersistenceService(
    private val serviceRepository: RegistryServiceRepository,
    private val serviceGeneralInfoRepository: RegistryServiceGeneralInfoRepository,
    private val serviceOnecloudInfoRepository: RegistryServiceOnecloudInfoRepository,
    private val serviceDetailsPersistenceService: RegistryServiceDetailsPersistenceService,
    private val serviceMongodbInfoRepository: RegistryServiceMongodbInfoRepository,
    private val onecloudDatacenterPersistenceService: RegistryOnecloudDatacenterPersistenceService,
    private val teamcityProjectPersistenceService: RegistryTeamcityProjectPersistenceService,
    private val mdbMongoCredentialsFactory: MdbMongoCredentialsFactory,
) {

    @Transactional
    fun syncServices(
        scope: RegistryServiceScope,
        services: List<ServiceRuntime>,
        dcSettings: RecommenderDcSettings,
    ) {
        syncServicesInScope(
            scope = scope,
            existingServices = findServices(scope),
            serviceRuntimes = services,
            dcSettings = dcSettings,
        )
    }

    private fun syncServicesInScope(
        scope: RegistryServiceScope,
        existingServices: List<RegistryServiceEntity>,
        serviceRuntimes: List<ServiceRuntime>,
        dcSettings: RecommenderDcSettings,
    ) {
        val existingByType = existingServices.associateBy(RegistryServiceEntity::serviceType)
        val incomingTypes = serviceRuntimes.map(ServiceRuntime::type).toSet()

        deleteOldServices(
            existingServices = existingServices,
            incomingTypes = incomingTypes,
        )

        serviceRuntimes.forEach { serviceRuntime ->
            val existingService = existingByType[serviceRuntime.type]
            val savedService = serviceRepository.save(
                RegistryServiceEntity(
                    uid = existingService?.uid,
                    cloudService = serviceRuntime.cloudServiceName,
                    serviceType = serviceRuntime.type,
                    projectUid = scope.projectUid,
                    verticalUid = scope.verticalUid,
                ),
            )

            upsertServiceGeneralInfo(savedService.requireId(), serviceRuntime)
            upsertServiceOnecloudInfo(savedService.requireId(), serviceRuntime)
            onecloudDatacenterPersistenceService.syncServiceDatacenters(
                serviceUid = savedService.requireId(),
                serviceRuntime = serviceRuntime,
                dcSettings = dcSettings,
            )
            teamcityProjectPersistenceService.syncService(savedService.requireId(), serviceRuntime)
            serviceDetailsPersistenceService.sync(savedService.requireId(), serviceRuntime)
            upsertServiceMongodbInfo(savedService.requireId(), serviceRuntime)
        }
    }

    private fun upsertServiceGeneralInfo(
        serviceUid: UUID,
        serviceRuntime: ServiceRuntime,
    ) {
        val existingGeneralInfo = serviceGeneralInfoRepository.findByServiceUid(serviceUid)

        serviceGeneralInfoRepository.save(
            RegistryServiceGeneralInfoEntity(
                uid = existingGeneralInfo?.uid,
                serviceUid = serviceUid,
                pmsApplication = resolvePmsApplication(serviceRuntime),
                hermesGroup = serviceRuntime.cloudServiceName.takeIf { serviceRuntime.hasSnapshots },
                links = existingGeneralInfo?.links ?: REGISTRY_EMPTY_JSON,
            ),
        )
    }

    private fun findServices(scope: RegistryServiceScope): List<RegistryServiceEntity> {
        return scope.projectUid?.let(serviceRepository::findAllByProjectUid)
            ?: serviceRepository.findAllByVerticalUid(requireNotNull(scope.verticalUid))
    }

    private fun deleteOldServices(
        existingServices: List<RegistryServiceEntity>,
        incomingTypes: Set<ServiceType>,
    ) {
        val oldServices = existingServices.filterNot { existingService ->
            existingService.serviceType in incomingTypes
        }
        if (oldServices.isNotEmpty()) {
            serviceRepository.deleteAll(oldServices)
        }
    }

    private fun upsertServiceOnecloudInfo(
        serviceUid: UUID,
        serviceRuntime: ServiceRuntime,
    ) {
        val existingOnecloudInfo = serviceOnecloudInfoRepository.findByServiceUid(serviceUid)

        serviceOnecloudInfoRepository.save(
            RegistryServiceOnecloudInfoEntity(
                uid = existingOnecloudInfo?.uid,
                serviceUid = serviceUid,
                cloudServiceId = existingOnecloudInfo?.cloudServiceId,
                productionQueue = serviceRuntime.cloudQueueId(ServiceEnvironment.PRODUCTION),
                canaryQueue = serviceRuntime
                    .takeIf { runtime -> runtime.supports(ServiceEnvironment.CANARY) }
                    ?.cloudQueueId(ServiceEnvironment.CANARY),
                testingQueue = serviceRuntime
                    .takeIf { runtime -> runtime.supports(ServiceEnvironment.TESTING) }
                    ?.cloudQueueId(ServiceEnvironment.TESTING),
                subqueues = serviceRuntime.distinctOnecloudSubqueues().toTypedArray(),
            ),
        )
    }

    private fun resolvePmsApplication(serviceRuntime: ServiceRuntime): String? {
        return serviceRuntime.pmsRootQueueName
            .takeIf(String::isNotBlank)
            ?.let { serviceRuntime.pmsApplicationName }
    }

    private fun upsertServiceMongodbInfo(
        serviceUid: UUID,
        serviceRuntime: ServiceRuntime,
    ) {
        val existingMongodbInfo = serviceMongodbInfoRepository.findByServiceUid(serviceUid)

        if (serviceRuntime.type != ServiceType.SNAPSHOTS_BUILDER) {
            existingMongodbInfo?.let(serviceMongodbInfoRepository::delete)
            return
        }

        val mongoCredentials = mdbMongoCredentialsFactory.create(serviceRuntime.cloudServiceName)
        serviceMongodbInfoRepository.save(
            RegistryServiceMongodbInfoEntity(
                uid = existingMongodbInfo?.uid,
                serviceUid = serviceUid,
                databaseName = mongoCredentials.databaseName,
                userName = mongoCredentials.userName,
            ),
        )
    }
}
