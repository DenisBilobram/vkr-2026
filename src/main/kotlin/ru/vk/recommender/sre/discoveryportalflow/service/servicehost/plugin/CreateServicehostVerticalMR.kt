package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.context.ServicehostTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.service.mr.ServicehostVerticalMrService

class CreateServicehostVerticalMR(
    private val servicehostVerticalMrService: ServicehostVerticalMrService,
) : FlowTask<ServicehostTaskContext>(ServicehostTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: ServicehostTaskContext): TaskRunResult {
        val configApplyResult = servicehostVerticalMrService.createVerticalMr(taskRunContext)
        if (configApplyResult == null) {
            runtimeLogger.info(
                "Skip online servicehost config for ${taskRunContext.recommenderName}: no online graph services found",
            )
            return TaskRunResult(taskStatus = FlowStatus.SKIPPED)
        }

        runtimeLogger.info(
            "Applied online servicehost cluster config for ${taskRunContext.recommenderName}: " +
                "${configApplyResult.fullClusterName}, mrUrl=${configApplyResult.configMrUrl}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
