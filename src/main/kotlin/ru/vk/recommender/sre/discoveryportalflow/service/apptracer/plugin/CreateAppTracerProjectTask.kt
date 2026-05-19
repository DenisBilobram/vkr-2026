package ru.vk.recommender.sre.discoveryportalflow.service.apptracer.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.context.CreateAppTracerProjectContext
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.service.AppTracerProjectService
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.service.ProjectSetupResult
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult

class CreateAppTracerProjectTask(
    private val appTracerProjectService: AppTracerProjectService,
) : FlowTask<CreateAppTracerProjectContext>(CreateAppTracerProjectContext::class) {

    override suspend fun executeCasted(taskRunContext: CreateAppTracerProjectContext): TaskRunResult {
        val recommenderName = taskRunContext.recommender.recommenderName
        val ownerLogin = taskRunContext.recommender.serviceOwner.substringBefore('@')
        runtimeLogger.info("Creating AppTracer project for $recommenderName, orgId=${taskRunContext.orgId}")

        return when (
            val result = appTracerProjectService.createOrGetApp(
                projectName = recommenderName,
                orgId = taskRunContext.orgId,
                idmRoleOwners = listOf(ownerLogin),
            )
        ) {
            is ProjectSetupResult.Success -> {
                taskRunContext.apptracerToken = result.token
                runtimeLogger.info("Stored AppTracer token for $recommenderName")
                TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
            }

            is ProjectSetupResult.Error -> {
                val causeMessage = result.cause?.message?.let { ": $it" }.orEmpty()
                runtimeLogger.error("${result.message}$causeMessage")
                TaskRunResult(taskStatus = FlowStatus.FAILED)
            }
        }
    }
}
