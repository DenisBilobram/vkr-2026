package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.project.RegistryProjectGeneralInfoEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.onecloud.RegistryProjectOnecloudInfoEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.project.RegistryProjectEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.project.RegistryProjectGeneralInfoRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.integrations.onecloud.RegistryProjectOnecloudInfoRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.project.RegistryProjectRepository
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.apptracer.RegistryApptracerProjectInfoPersistenceService
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.onecloud.RegistryOnecloudDatacenterPersistenceService
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.teamcity.RegistryTeamcityProjectPersistenceService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ProjectRecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.util.TogglesTenantNameResolver
import java.util.UUID

@Service
class RegistryProjectPersistenceService(
    private val projectRepository: RegistryProjectRepository,
    private val projectGeneralInfoRepository: RegistryProjectGeneralInfoRepository,
    private val projectOnecloudInfoRepository: RegistryProjectOnecloudInfoRepository,
    private val onecloudDatacenterPersistenceService: RegistryOnecloudDatacenterPersistenceService,
    private val apptracerProjectInfoPersistenceService: RegistryApptracerProjectInfoPersistenceService,
    private val teamcityProjectPersistenceService: RegistryTeamcityProjectPersistenceService,
    private val registryServicePersistenceService: RegistryServicePersistenceService,
) {

    @Transactional
    fun syncProject(
        projectRecommenderRuntime: ProjectRecommenderRuntime,
        dcSettings: RecommenderDcSettings,
    ) {
        require(projectRecommenderRuntime.services.all { it.scope == ServiceScope.PROJECT_SCOPED }) {
            "Project registry sync accepts only project-scoped services"
        }

        val project = ensureProject(projectRecommenderRuntime, dcSettings)
        upsertProjectGeneralInfo(project.requireId(), projectRecommenderRuntime)

        registryServicePersistenceService.syncServices(
            scope = RegistryServiceScope.project(project.requireId()),
            services = projectRecommenderRuntime.services,
            dcSettings = dcSettings,
        )
    }

    @Transactional(readOnly = true)
    fun requireProject(projectName: String): RegistryProjectEntity {
        return requireNotNull(projectRepository.findByProjectName(projectName)) {
            "Registry project '$projectName' is not registered"
        }
    }

    private fun ensureProject(
        projectRecommenderRuntime: ProjectRecommenderRuntime,
        dcSettings: RecommenderDcSettings,
    ): RegistryProjectEntity {
        val projectName = projectRecommenderRuntime.projectName
        val existingProject = projectRepository.findByProjectName(projectName)
        val savedProject = projectRepository.save(
            RegistryProjectEntity(uid = existingProject?.uid, projectName = projectName),
        )
        upsertProjectOnecloudInfo(savedProject.requireId(), projectRecommenderRuntime)
        apptracerProjectInfoPersistenceService.syncProject(savedProject.requireId(), projectName)
        teamcityProjectPersistenceService.syncProject(savedProject.requireId(), projectRecommenderRuntime)
        onecloudDatacenterPersistenceService.syncProjectDatacenters(savedProject.requireId(), dcSettings)
        return savedProject
    }

    private fun upsertProjectGeneralInfo(
        projectUid: UUID,
        projectRecommenderRuntime: ProjectRecommenderRuntime,
    ) {
        val existingGeneralInfo = projectGeneralInfoRepository.findByProjectUid(projectUid)
        projectGeneralInfoRepository.save(
            RegistryProjectGeneralInfoEntity(
                uid = existingGeneralInfo?.uid,
                projectUid = projectUid,
                displayName = existingGeneralInfo?.displayName ?: projectRecommenderRuntime.projectName,
                productId = projectRecommenderRuntime.productId ?: existingGeneralInfo?.productId,
                prm = existingGeneralInfo?.prm,
                togglesOfflineTenant = TogglesTenantNameResolver.resolveProjectOfflineTenantName(
                    projectRecommenderRuntime.projectName,
                ),
                links = existingGeneralInfo?.links ?: REGISTRY_EMPTY_JSON,
            ),
        )
    }

    private fun upsertProjectOnecloudInfo(
        projectUid: UUID,
        projectRecommenderRuntime: ProjectRecommenderRuntime,
    ) {
        val existingOnecloudInfo = projectOnecloudInfoRepository.findByProjectUid(projectUid)
        projectOnecloudInfoRepository.save(
            RegistryProjectOnecloudInfoEntity(
                uid = existingOnecloudInfo?.uid,
                projectUid = projectUid,
                cloudNamespace = existingOnecloudInfo?.cloudNamespace ?: REGISTRY_DEFAULT_CLOUD_NAMESPACE,
                productionRootQueue = projectRecommenderRuntime.productionRootQueueName,
                testingRootQueue = projectRecommenderRuntime.testingRootQueueName,
            ),
        )
    }
}
