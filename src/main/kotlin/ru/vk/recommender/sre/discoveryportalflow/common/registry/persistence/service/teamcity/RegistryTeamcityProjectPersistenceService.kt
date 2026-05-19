package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.teamcity

import java.util.UUID
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.teamcity.RegistryTeamcityProjectEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.integrations.teamcity.RegistryTeamcityProjectRepository
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ProjectRecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.utils.TeamcityProjectIdResolver

@Service
class RegistryTeamcityProjectPersistenceService(
    private val teamcityProjectRepository: RegistryTeamcityProjectRepository,
) {

    fun syncProject(
        projectUid: UUID,
        projectRecommenderRuntime: ProjectRecommenderRuntime,
    ) {
        syncProject(projectUid, TeamcityProjectIdResolver.resolveProjectProjectId(projectRecommenderRuntime))
    }

    private fun syncProject(
        projectUid: UUID,
        teamcityProjectId: String,
    ) {
        val existingProject = teamcityProjectRepository.findByProjectUid(projectUid)
        teamcityProjectRepository.save(
            RegistryTeamcityProjectEntity(
                uid = existingProject?.uid,
                projectUid = projectUid,
                verticalUid = null,
                serviceUid = null,
                teamcityProjectId = teamcityProjectId,
            ),
        )
    }

    fun syncVertical(
        verticalUid: UUID,
        recommenderRuntime: RecommenderRuntime,
    ) {
        val existingProject = teamcityProjectRepository.findByVerticalUid(verticalUid)
        teamcityProjectRepository.save(
            RegistryTeamcityProjectEntity(
                uid = existingProject?.uid,
                projectUid = null,
                verticalUid = verticalUid,
                serviceUid = null,
                teamcityProjectId = TeamcityProjectIdResolver.resolveVerticalProjectId(recommenderRuntime),
            ),
        )
    }

    fun syncService(
        serviceUid: UUID,
        serviceRuntime: ServiceRuntime,
    ) {
        val existingProject = teamcityProjectRepository.findByServiceUid(serviceUid)
        teamcityProjectRepository.save(
            RegistryTeamcityProjectEntity(
                uid = existingProject?.uid,
                projectUid = null,
                verticalUid = null,
                serviceUid = serviceUid,
                teamcityProjectId = TeamcityProjectIdResolver.resolveServiceProjectId(serviceRuntime),
            ),
        )
    }
}
