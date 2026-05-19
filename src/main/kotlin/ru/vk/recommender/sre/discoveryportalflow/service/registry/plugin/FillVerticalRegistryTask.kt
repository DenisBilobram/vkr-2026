package ru.vk.recommender.sre.discoveryportalflow.service.registry.plugin

import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.RegistryVerticalPersistenceService
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope
import ru.vk.recommender.sre.discoveryportalflow.service.registry.context.FillVerticalRegistryTaskContext

class FillVerticalRegistryTask(
    private val registryVerticalPersistenceService: RegistryVerticalPersistenceService,
) : FlowTask<FillVerticalRegistryTaskContext>(FillVerticalRegistryTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: FillVerticalRegistryTaskContext): TaskRunResult {
        val verticalServices = taskRunContext.services.filter { serviceRuntime ->
            serviceRuntime.scope != ServiceScope.PROJECT_SCOPED
        }

        registryVerticalPersistenceService.syncVertical(
            recommenderRuntime = taskRunContext.recommender,
            dcSettings = taskRunContext.dcSettings,
            services = verticalServices,
            teamsChatId = taskRunContext.teamsChatId,
            ytCluster = taskRunContext.ytCluster.name,
        )

        runtimeLogger.info(
            "Filled registry vertical '${taskRunContext.recommender.recommenderName}' with ${verticalServices.size} services",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
