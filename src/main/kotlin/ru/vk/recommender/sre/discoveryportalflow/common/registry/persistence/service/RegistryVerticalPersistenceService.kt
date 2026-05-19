package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.apptracer.RegistryApptracerProjectInfoPersistenceService
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.RegistryVerticalOnecloudInfoEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.vertical.RegistryVerticalEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.vertical.RegistryVerticalGeneralInfoEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.integrations.onecloud.RegistryVerticalOnecloudInfoRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.vertical.RegistryVerticalGeneralInfoRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.vertical.RegistryVerticalRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.onecloud.RegistryOnecloudDatacenterPersistenceService
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.teamcity.RegistryTeamcityProjectPersistenceService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.util.TogglesTenantNameResolver
import java.util.UUID

@Service
class RegistryVerticalPersistenceService(
    private val registryProjectPersistenceService: RegistryProjectPersistenceService,
    private val verticalRepository: RegistryVerticalRepository,
    private val verticalGeneralInfoRepository: RegistryVerticalGeneralInfoRepository,
    private val verticalOnecloudInfoRepository: RegistryVerticalOnecloudInfoRepository,
    private val onecloudDatacenterPersistenceService: RegistryOnecloudDatacenterPersistenceService,
    private val apptracerProjectInfoPersistenceService: RegistryApptracerProjectInfoPersistenceService,
    private val teamcityProjectPersistenceService: RegistryTeamcityProjectPersistenceService,
    private val registryServicePersistenceService: RegistryServicePersistenceService,
) {

    @Transactional
    fun syncVertical(
        recommenderRuntime: RecommenderRuntime,
        dcSettings: RecommenderDcSettings,
        services: List<ServiceRuntime>,
        teamsChatId: String?,
        ytCluster: String,
    ) {
        require(services.none { serviceRuntime -> serviceRuntime.scope == ServiceScope.PROJECT_SCOPED }) {
            "Vertical registry sync accepts only vertical-scoped services"
        }

        val projectName = requireNotNull(recommenderRuntime.projectName) {
            "Vertical registry sync requires recommender.projectName"
        }
        val project = registryProjectPersistenceService.requireProject(projectName)

        val existingVertical = verticalRepository.findByProjectUidAndVerticalName(
            projectUid = project.requireId(),
            verticalName = recommenderRuntime.recommenderName,
        )

        val savedVertical = verticalRepository.save(
            RegistryVerticalEntity(
                uid = existingVertical?.uid,
                projectUid = project.requireId(),
                verticalName = recommenderRuntime.recommenderName,
                releaseStatus = existingVertical?.releaseStatus ?: DEFAULT_RELEASE_STATUS,
            ),
        )

        upsertVerticalGeneralInfo(
            verticalUid = savedVertical.requireId(),
            recommenderRuntime = recommenderRuntime,
            teamsChatId = teamsChatId,
            ytCluster = ytCluster,
        )
        upsertVerticalOnecloudInfo(
            verticalUid = savedVertical.requireId(),
            recommenderRuntime = recommenderRuntime,
        )
        onecloudDatacenterPersistenceService.syncVerticalDatacenters(savedVertical.requireId(), dcSettings)
        apptracerProjectInfoPersistenceService.syncVertical(savedVertical.requireId(), recommenderRuntime.recommenderName)
        teamcityProjectPersistenceService.syncVertical(savedVertical.requireId(), recommenderRuntime)

        registryServicePersistenceService.syncServices(
            scope = RegistryServiceScope.vertical(savedVertical.requireId()),
            services = services,
            dcSettings = dcSettings,
        )
    }

    private fun upsertVerticalGeneralInfo(
        verticalUid: UUID,
        recommenderRuntime: RecommenderRuntime,
        teamsChatId: String?,
        ytCluster: String,
    ) {
        val existingGeneralInfo = verticalGeneralInfoRepository.findByVerticalUid(verticalUid)

        verticalGeneralInfoRepository.save(
            RegistryVerticalGeneralInfoEntity(
                uid = existingGeneralInfo?.uid,
                verticalUid = verticalUid,
                displayName = existingGeneralInfo?.displayName ?: recommenderRuntime.recommenderName,
                serviceOwner = recommenderRuntime.serviceOwner,
                productId = recommenderRuntime.productId ?: existingGeneralInfo?.productId,
                prm = existingGeneralInfo?.prm,
                hermesProjectName = existingGeneralInfo?.hermesProjectName ?: recommenderRuntime.recommenderName,
                dictionaryBaseProject = recommenderRuntime.dictionaryBaseProject,
                additionalResponsibles = recommenderRuntime.additionalResponsibles.toRegistryTextArray(),
                additionalFollowers = recommenderRuntime.additionalFollowers.toRegistryTextArray(),
                servicehostClusterName = recommenderRuntime.clusterName,
                teamsChatId = teamsChatId ?: existingGeneralInfo?.teamsChatId,
                ytCluster = ytCluster,
                togglesOnlineTenant = TogglesTenantNameResolver.resolveOnlineTenantName(recommenderRuntime.recommenderName),
                togglesOfflineTenant = TogglesTenantNameResolver.resolveVerticalOfflineTenantName(
                    recommenderRuntime.recommenderName,
                ),
                links = existingGeneralInfo?.links ?: REGISTRY_EMPTY_JSON,
            ),
        )
    }

    private fun upsertVerticalOnecloudInfo(
        verticalUid: UUID,
        recommenderRuntime: RecommenderRuntime,
    ) {
        val existingOnecloudInfo = verticalOnecloudInfoRepository.findByVerticalUid(verticalUid)

        verticalOnecloudInfoRepository.save(
            RegistryVerticalOnecloudInfoEntity(
                uid = existingOnecloudInfo?.uid,
                verticalUid = verticalUid,
                productionRootQueue = recommenderRuntime.productionRootQueueName,
                testingRootQueue = recommenderRuntime.testingRootQueueName,
                i2iProductionRootQueue = recommenderRuntime.i2iProductionRootQueueName,
            ),
        )
    }

    private companion object {
        private const val DEFAULT_RELEASE_STATUS = "prod"
    }
}
