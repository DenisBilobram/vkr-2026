package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.context.TeamcityProjectsTaskContext

@JsonIgnoreProperties(ignoreUnknown = true)
data class OnecloudDeploymentTaskContext(
    val teamcityProjectsTaskContext: TeamcityProjectsTaskContext,
    val onecloudSubmitQueuesTaskContext: OnecloudSubmitQueuesTaskContext,
    val onecloudSubmitStoragesTaskContext: OnecloudSubmitStoragesTaskContext,
    val onecloudSubmitServicesTaskContext: OnecloudSubmitServicesTaskContext,
    val onecloudWaitServicesRunningTaskContext: OnecloudWaitServicesRunningTaskContext,
) : FlowTaskContext
