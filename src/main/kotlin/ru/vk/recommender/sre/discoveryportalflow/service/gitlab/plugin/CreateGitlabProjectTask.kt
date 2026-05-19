package ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabProjectTaskContext

class CreateGitlabProjectTask(
    private val gitlabClient: GitlabClient,
) : FlowTask<GitlabProjectTaskContext>(GitlabProjectTaskContext::class) {

    override suspend fun executeCasted(taskRunContext: GitlabProjectTaskContext): TaskRunResult {
        val projectInfo = taskRunContext.gitlabProjectInfo
        val project = try {
            gitlabClient.createProject(
                name = projectInfo.gitlabProjectName,
                path = projectInfo.gitlabProjectPath,
            )
        } catch (exception: Exception) {
            if (exception !is java.io.IOException || !gitlabClient.isProjectAlreadyExistsError(exception)) {
                throw exception
            }

            requireNotNull(gitlabClient.getProjectByPath(projectInfo.gitlabRepositoryPath)) {
                "GitLab project '${projectInfo.gitlabRepositoryPath}' already exists but cannot be resolved"
            }
        }

        taskRunContext.resolveProject(project.id, project.webUrl)
        runtimeLogger.info(
            "Resolved GitLab project for ${projectInfo.gitlabRepositoryPath}: id=${project.id}, url=${project.webUrl ?: "n/a"}",
        )
        return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
    }
}
