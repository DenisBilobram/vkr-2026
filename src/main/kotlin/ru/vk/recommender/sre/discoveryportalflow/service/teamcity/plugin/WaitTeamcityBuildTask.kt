package ru.vk.recommender.sre.discoveryportalflow.service.teamcity.plugin

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowWaitingTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.client.TeamCityClient
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.context.TeamcityProjectsTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.models.TriggeredBuild

@Component
class WaitTeamcityBuildTask(
    private val teamCityClient: TeamCityClient,
) : FlowWaitingTask<TeamcityProjectsTaskContext>(TeamcityProjectsTaskContext::class) {

    override suspend fun check(taskRunContext: TeamcityProjectsTaskContext): TaskRunResult {
        runtimeLogger.info("=".repeat(150))
        runtimeLogger.info("Check teamcity projects")
        val builds = taskRunContext.builds

        val completedBuilds = mutableSetOf<TriggeredBuild>()
        val runningBuilds = mutableSetOf<TriggeredBuild>()
        val failedBuilds = mutableSetOf<TriggeredBuild>()

        for (build in builds) {
            val buildById = teamCityClient.getBuildById(build.queueId)
            if (buildById == null || buildById.isRunning()) {
                runtimeLogger.info("Running build: ${buildById?.webUrl}")
                runningBuilds.add(build)
                continue
            }
            if (buildById.isCompleted()) {
                runtimeLogger.info("Completed build: ${buildById.webUrl}")
                completedBuilds.add(build)
                continue
            }
            if (buildById.isFailed()) {
                runtimeLogger.info("Failed build: ${buildById.webUrl}")
                failedBuilds.add(build)
            }
        }

        if (failedBuilds.isEmpty() && runningBuilds.isEmpty() && completedBuilds.size == builds.size) {
            runtimeLogger.info("All builds finished")
            runtimeLogger.info("=".repeat(150))
            return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
        }

        if (failedBuilds.isNotEmpty()) {
            taskRunContext.builds.clear()
            taskRunContext.builds.addAll(runningBuilds)
            taskRunContext.builds.addAll(completedBuilds)
            failedBuilds.forEach {
                val triggerBuild = teamCityClient.triggerBuild(it.dockerBuildId, it.branch)
                if (triggerBuild == null) {
                    runtimeLogger.error("Cannot create build for id=${it.dockerBuildId}, branch=${it.branch}")
                    taskRunContext.builds.add(it)
                } else {
                    taskRunContext.builds.add(TriggeredBuild(it.dockerBuildId, it.branch, triggerBuild))
                }
            }
            runtimeLogger.error("Has failed builds: $failedBuilds")
            runtimeLogger.info("=".repeat(150))

            return TaskRunResult(taskStatus = FlowStatus.FAILED_WITH_RETRY)
        }

        runtimeLogger.info("Waiting for all builds")
        runtimeLogger.info("=".repeat(150))
        return TaskRunResult(taskStatus = FlowStatus.WAITING)
    }
}

