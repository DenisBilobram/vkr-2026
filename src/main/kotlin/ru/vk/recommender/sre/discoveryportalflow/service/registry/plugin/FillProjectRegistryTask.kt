package ru.vk.recommender.sre.discoveryportalflow.service.registry.plugin

import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.RegistryProjectPersistenceService
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.registry.context.FillProjectRegistryTaskContext

class FillProjectRegistryTask(
    private val registryProjectPersistenceService: RegistryProjectPersistenceService,
) : FlowTask<FillProjectRegistryTaskContext>(FillProjectRegistryTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: FillProjectRegistryTaskContext): TaskRunResult {
        registryProjectPersistenceService.syncProject(
            projectRecommenderRuntime = taskRunContext.projectRecommender,
            dcSettings = taskRunContext.dcSettings,
        )

        runtimeLogger.info(
            "Filled registry project '${taskRunContext.projectRecommender.projectName}' " +
                "with ${taskRunContext.projectRecommender.services.size} services",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
