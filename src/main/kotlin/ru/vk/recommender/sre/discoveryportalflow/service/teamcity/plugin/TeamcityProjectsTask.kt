package ru.vk.recommender.sre.discoveryportalflow.service.teamcity.plugin

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.enabledServices
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.context.TeamcityProjectsTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.orchestration.TaskCreator
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.utils.NameUtils.buildProjectParameters
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.utils.NameUtils.buildTeamcitySubprojectNames

@Component
class TeamcityProjectsTask(
    private val taskCreator: TaskCreator,
) : FlowTask<TeamcityProjectsTaskContext>(TeamcityProjectsTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: TeamcityProjectsTaskContext): TaskRunResult {
        var createdProjects = 0
        val createdProjectServiceNames = mutableListOf<String>()
        taskRunContext.services.enabledServices().forEach { serviceRuntime ->
            val projectCreated = generateTeamcityProject(
                taskContext = taskRunContext,
                serviceRuntime = serviceRuntime,
                taskCreator = taskCreator,
            )
            if (projectCreated) {
                createdProjects++
                createdProjectServiceNames += serviceRuntime.cloudServiceName
            }
        }

        if (createdProjects == 0) {
            runtimeLogger.info("Skip TeamCity project provisioning: no service requested project creation")
        }
        val jiraComponents = createdProjectServiceNames
            .distinct()
            .sorted()
        if (jiraComponents.isNotEmpty()) {
            taskCreator.addComponents(jiraComponents)
            runtimeLogger.info(
                "Requested Jira component creation for ${taskRunContext.recommenderName}: " +
                    jiraComponents.joinToString(),
            )
        }

        runtimeLogger.info(
            "Processed TeamCity project provisioning for ${taskRunContext.recommenderName}: " +
                "created=$createdProjects",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }

    private suspend fun generateTeamcityProject(
        taskContext: TeamcityProjectsTaskContext,
        serviceRuntime: ServiceRuntime,
        taskCreator: TaskCreator,
    ): Boolean {
        val projectParameters = buildProjectParameters(taskContext, serviceRuntime)

        val triggeredBuild = taskCreator.createServiceProject(
            serviceName = serviceRuntime.cloudServiceName,
            subprojectNames = buildTeamcitySubprojectNames(taskContext),
            prodDcs = taskContext.dcSettings.productionDcs.distinct(),
            testDcs = if (serviceRuntime.hasTesting) taskContext.dcSettings.testingDcs.distinct() else emptyList(),
            canaryDcs = if (serviceRuntime.hasCanary) taskContext.dcSettings.canaryDcs.distinct() else emptyList(),
            additionalProps = projectParameters,
            branch = taskContext.branch,
        )
        taskContext.builds.add(triggeredBuild)

        runtimeLogger.info("Created TeamCity project for ${serviceRuntime.cloudServiceName}")
        return true
    }
}
