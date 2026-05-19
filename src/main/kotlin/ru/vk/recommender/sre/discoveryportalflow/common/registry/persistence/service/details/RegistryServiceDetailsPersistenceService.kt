package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.details

import java.util.UUID
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceBaseDetailsEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceBaseI2IDetailsEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceFactorProxyDetailsEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceGatewayDetailsEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceGrpcProxyDetailsEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceMediatorDetailsEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceMetaDetailsEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceMetaI2IDetailsEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServicePlatformGatewayDetailsEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceSchedulerI2IDetailsEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceSelectorsDetailsEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceSnapshotsBuilderDetailsEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceWorkerDetailsEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.service.details.RegistryServiceYtProxyDetailsEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details.RegistryServiceBaseDetailsRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details.RegistryServiceBaseI2IDetailsRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details.RegistryServiceFactorProxyDetailsRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details.RegistryServiceGatewayDetailsRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details.RegistryServiceGrpcProxyDetailsRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details.RegistryServiceMediatorDetailsRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details.RegistryServiceMetaDetailsRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details.RegistryServiceMetaI2IDetailsRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details.RegistryServicePlatformGatewayDetailsRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details.RegistryServiceSchedulerI2IDetailsRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details.RegistryServiceSelectorsDetailsRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details.RegistryServiceSnapshotsBuilderDetailsRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details.RegistryServiceWorkerDetailsRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.service.details.RegistryServiceYtProxyDetailsRepository
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.BaseI2IServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.BaseServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.MediatorServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.PlatformGatewayServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.WorkerServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

private typealias DetailsSyncer = (UUID, ServiceRuntime) -> Unit

@Service
class RegistryServiceDetailsPersistenceService(
    private val serviceBaseDetailsRepository: RegistryServiceBaseDetailsRepository,
    private val serviceMetaDetailsRepository: RegistryServiceMetaDetailsRepository,
    private val serviceGatewayDetailsRepository: RegistryServiceGatewayDetailsRepository,
    private val servicePlatformGatewayDetailsRepository: RegistryServicePlatformGatewayDetailsRepository,
    private val serviceGrpcProxyDetailsRepository: RegistryServiceGrpcProxyDetailsRepository,
    private val serviceMediatorDetailsRepository: RegistryServiceMediatorDetailsRepository,
    private val serviceYtProxyDetailsRepository: RegistryServiceYtProxyDetailsRepository,
    private val serviceFactorProxyDetailsRepository: RegistryServiceFactorProxyDetailsRepository,
    private val serviceSelectorsDetailsRepository: RegistryServiceSelectorsDetailsRepository,
    private val serviceWorkerDetailsRepository: RegistryServiceWorkerDetailsRepository,
    private val serviceSnapshotsBuilderDetailsRepository: RegistryServiceSnapshotsBuilderDetailsRepository,
    private val serviceMetaI2IDetailsRepository: RegistryServiceMetaI2IDetailsRepository,
    private val serviceBaseI2IDetailsRepository: RegistryServiceBaseI2IDetailsRepository,
    private val serviceSchedulerI2IDetailsRepository: RegistryServiceSchedulerI2IDetailsRepository,
) {

    private val detailsSyncers: Map<ServiceType, DetailsSyncer> = mapOf(
        ServiceType.BASE to ::syncBaseDetails,
        ServiceType.META to { serviceUid, _ -> syncMetaDetails(serviceUid) },
        ServiceType.GATEWAY to { serviceUid, _ -> syncGatewayDetails(serviceUid) },
        ServiceType.PLATFORM_GATEWAY to ::syncPlatformGatewayDetails,
        ServiceType.GRPC_PROXY to { serviceUid, _ -> syncGrpcProxyDetails(serviceUid) },
        ServiceType.MEDIATOR to ::syncMediatorDetails,
        ServiceType.YT_PROXY to ::syncYtProxyDetails,
        ServiceType.FACTOR_PROXY to { serviceUid, _ -> syncFactorProxyDetails(serviceUid) },
        ServiceType.SELECTORS to { serviceUid, _ -> syncSelectorsDetails(serviceUid) },
        ServiceType.WORKER to ::syncWorkerDetails,
        ServiceType.SNAPSHOTS_BUILDER to { serviceUid, _ -> syncSnapshotsBuilderDetails(serviceUid) },
        ServiceType.META_I2I to { serviceUid, _ -> syncMetaI2IDetails(serviceUid) },
        ServiceType.BASE_I2I to ::syncBaseI2IDetails,
        ServiceType.SCHEDULER_I2I to { serviceUid, _ -> syncSchedulerI2IDetails(serviceUid) },
    )

    fun sync(
        serviceUid: UUID,
        serviceRuntime: ServiceRuntime,
    ) {
        detailsSyncers.getValue(serviceRuntime.type).invoke(serviceUid, serviceRuntime)
    }

    private fun syncBaseDetails(serviceUid: UUID, serviceRuntime: ServiceRuntime) {
        serviceBaseDetailsRepository.save(
            RegistryServiceBaseDetailsEntity(
                uid = serviceBaseDetailsRepository.findByServiceUid(serviceUid)?.uid,
                serviceUid = serviceUid,
                shardsCount = serviceRuntime.config.requireConfig(BaseServiceConfig::class).shardsCount,
            ),
        )
    }

    private fun syncMetaDetails(serviceUid: UUID) {
        serviceMetaDetailsRepository.save(
            RegistryServiceMetaDetailsEntity(
                uid = serviceMetaDetailsRepository.findByServiceUid(serviceUid)?.uid,
                serviceUid = serviceUid,
            ),
        )
    }

    private fun syncGatewayDetails(serviceUid: UUID) {
        serviceGatewayDetailsRepository.save(
            RegistryServiceGatewayDetailsEntity(
                uid = serviceGatewayDetailsRepository.findByServiceUid(serviceUid)?.uid,
                serviceUid = serviceUid,
            ),
        )
    }

    private fun syncPlatformGatewayDetails(serviceUid: UUID, serviceRuntime: ServiceRuntime) {
        servicePlatformGatewayDetailsRepository.save(
            RegistryServicePlatformGatewayDetailsEntity(
                uid = servicePlatformGatewayDetailsRepository.findByServiceUid(serviceUid)?.uid,
                serviceUid = serviceUid,
                baseShardsAmount = serviceRuntime.config
                    .requireConfig(PlatformGatewayServiceConfig::class).baseShardsAmount,
            ),
        )
    }

    private fun syncGrpcProxyDetails(serviceUid: UUID) {
        serviceGrpcProxyDetailsRepository.save(
            RegistryServiceGrpcProxyDetailsEntity(
                uid = serviceGrpcProxyDetailsRepository.findByServiceUid(serviceUid)?.uid,
                serviceUid = serviceUid,
            ),
        )
    }

    private fun syncMediatorDetails(serviceUid: UUID, serviceRuntime: ServiceRuntime) {
        serviceMediatorDetailsRepository.save(
            RegistryServiceMediatorDetailsEntity(
                uid = serviceMediatorDetailsRepository.findByServiceUid(serviceUid)?.uid,
                serviceUid = serviceUid,
                withCache = serviceRuntime.config.requireConfig(MediatorServiceConfig::class).withCache,
            ),
        )
    }

    private fun syncYtProxyDetails(serviceUid: UUID, serviceRuntime: ServiceRuntime) {
        serviceYtProxyDetailsRepository.save(
            RegistryServiceYtProxyDetailsEntity(
                uid = serviceYtProxyDetailsRepository.findByServiceUid(serviceUid)?.uid,
                serviceUid = serviceUid,
                tenant = serviceRuntime.tenant,
            ),
        )
    }

    private fun syncFactorProxyDetails(serviceUid: UUID) {
        serviceFactorProxyDetailsRepository.save(
            RegistryServiceFactorProxyDetailsEntity(
                uid = serviceFactorProxyDetailsRepository.findByServiceUid(serviceUid)?.uid,
                serviceUid = serviceUid,
            ),
        )
    }

    private fun syncSelectorsDetails(serviceUid: UUID) {
        serviceSelectorsDetailsRepository.save(
            RegistryServiceSelectorsDetailsEntity(
                uid = serviceSelectorsDetailsRepository.findByServiceUid(serviceUid)?.uid,
                serviceUid = serviceUid,
            ),
        )
    }

    private fun syncWorkerDetails(serviceUid: UUID, serviceRuntime: ServiceRuntime) {
        serviceWorkerDetailsRepository.save(
            RegistryServiceWorkerDetailsEntity(
                uid = serviceWorkerDetailsRepository.findByServiceUid(serviceUid)?.uid,
                serviceUid = serviceUid,
                defaultPool = serviceRuntime.config.requireConfig(WorkerServiceConfig::class).defaultPool,
            ),
        )
    }

    private fun syncSnapshotsBuilderDetails(serviceUid: UUID) {
        serviceSnapshotsBuilderDetailsRepository.save(
            RegistryServiceSnapshotsBuilderDetailsEntity(
                uid = serviceSnapshotsBuilderDetailsRepository.findByServiceUid(serviceUid)?.uid,
                serviceUid = serviceUid,
            ),
        )
    }

    private fun syncMetaI2IDetails(serviceUid: UUID) {
        serviceMetaI2IDetailsRepository.save(
            RegistryServiceMetaI2IDetailsEntity(
                uid = serviceMetaI2IDetailsRepository.findByServiceUid(serviceUid)?.uid,
                serviceUid = serviceUid,
            ),
        )
    }

    private fun syncBaseI2IDetails(serviceUid: UUID, serviceRuntime: ServiceRuntime) {
        serviceBaseI2IDetailsRepository.save(
            RegistryServiceBaseI2IDetailsEntity(
                uid = serviceBaseI2IDetailsRepository.findByServiceUid(serviceUid)?.uid,
                serviceUid = serviceUid,
                shardsCount = serviceRuntime.config.requireConfig(BaseI2IServiceConfig::class).shardsCount,
            ),
        )
    }

    private fun syncSchedulerI2IDetails(serviceUid: UUID) {
        serviceSchedulerI2IDetailsRepository.save(
            RegistryServiceSchedulerI2IDetailsEntity(
                uid = serviceSchedulerI2IDetailsRepository.findByServiceUid(serviceUid)?.uid,
                serviceUid = serviceUid,
            ),
        )
    }
}
