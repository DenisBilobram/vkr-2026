package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.apptracer

import java.util.UUID
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.integrations.apptracer.RegistryApptracerProjectInfoEntity
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.repository.integrations.apptracer.RegistryApptracerProjectInfoRepository
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.util.AppTracerProjectNameResolver

@Service
class RegistryApptracerProjectInfoPersistenceService(
    private val apptracerProjectInfoRepository: RegistryApptracerProjectInfoRepository,
) {

    fun syncProject(
        projectUid: UUID,
        projectName: String,
    ) {
        val existingProjectInfo = apptracerProjectInfoRepository.findByProjectUid(projectUid)
        apptracerProjectInfoRepository.save(
            RegistryApptracerProjectInfoEntity(
                uid = existingProjectInfo?.uid,
                apptracerProjectName = AppTracerProjectNameResolver.resolveProjectName(projectName),
                apptracerProjectId = existingProjectInfo?.apptracerProjectId,
                projectUid = projectUid,
                verticalUid = null,
            ),
        )
    }

    fun syncVertical(
        verticalUid: UUID,
        verticalName: String,
    ) {
        val existingProjectInfo = apptracerProjectInfoRepository.findByVerticalUid(verticalUid)
        apptracerProjectInfoRepository.save(
            RegistryApptracerProjectInfoEntity(
                uid = existingProjectInfo?.uid,
                apptracerProjectName = AppTracerProjectNameResolver.resolveProjectName(verticalName),
                apptracerProjectId = existingProjectInfo?.apptracerProjectId,
                projectUid = null,
                verticalUid = verticalUid,
            ),
        )
    }
}
