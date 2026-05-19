package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabProjectInfo

@JsonIgnoreProperties(ignoreUnknown = true)
data class GitlabProjectTaskContext(
    val gitlabProjectInfo: GitlabProjectInfo,
) : FlowTaskContext {

    fun resolveProject(projectId: Int, projectUrl: String?) {
        gitlabProjectInfo.gitlabProjectId = projectId
        gitlabProjectInfo.gitlabProjectUrl = projectUrl
    }
}
