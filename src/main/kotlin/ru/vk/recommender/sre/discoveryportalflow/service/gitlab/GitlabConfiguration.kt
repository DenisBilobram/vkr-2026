package ru.vk.recommender.sre.discoveryportalflow.service.gitlab

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskBean
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.config.GitlabProperties
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin.ApproveGitlabMergeRequestTask
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin.CommitGitlabFilesTask
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin.CreateGitlabMergeRequestTask
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin.CreateGitlabProjectTask
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin.CreateGitlabWebhookTask
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin.MergeGitlabMergeRequestTask
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin.WaitGitlabMergeRequestPipelineTask
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.plugin.WaitGitlabMrApprovePipelineTask
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.config.TogglesProperties

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GitlabProperties::class)
class GitlabConfiguration {

    @Bean
    fun gitlabClient(
        gitlabProperties: GitlabProperties,
    ): GitlabClient {
        return GitlabClient(gitlabProperties)
    }

    @FlowTaskBean(name = ["createGitlabProjectTask"])
    fun createGitlabProjectTask(
        gitlabClient: GitlabClient,
    ): CreateGitlabProjectTask {
        return CreateGitlabProjectTask(gitlabClient)
    }

    @FlowTaskBean(name = ["commitGitlabFilesTask"])
    fun commitGitlabFilesTask(
        gitlabClient: GitlabClient,
    ): CommitGitlabFilesTask {
        return CommitGitlabFilesTask(gitlabClient)
    }

    @FlowTaskBean(name = ["createGitlabWebhookTask"])
    fun createGitlabWebhookTask(
        gitlabClient: GitlabClient,
        togglesProperties: TogglesProperties,
    ): CreateGitlabWebhookTask {
        return CreateGitlabWebhookTask(gitlabClient, togglesProperties)
    }

    @FlowTaskBean(name = ["createGitlabMergeRequestTask"])
    fun createGitlabMergeRequestTask(
        gitlabClient: GitlabClient,
    ): CreateGitlabMergeRequestTask {
        return CreateGitlabMergeRequestTask(gitlabClient)
    }

    @FlowTaskBean(name = ["approveGitlabMergeRequestTask"])
    fun approveGitlabMergeRequestTask(
        gitlabClient: GitlabClient,
    ): ApproveGitlabMergeRequestTask {
        return ApproveGitlabMergeRequestTask(gitlabClient)
    }

    @FlowTaskBean(name = ["waitGitlabMergeRequestPipelineTask"])
    fun waitGitlabMergeRequestPipelineTask(
        gitlabClient: GitlabClient,
    ): WaitGitlabMergeRequestPipelineTask {
        return WaitGitlabMergeRequestPipelineTask(gitlabClient)
    }

    @FlowTaskBean(name = ["waitGitlabMrApprovePipelineTask"])
    fun waitGitlabMrApprovePipelineTask(
        gitlabClient: GitlabClient,
    ): WaitGitlabMrApprovePipelineTask {
        return WaitGitlabMrApprovePipelineTask(gitlabClient)
    }

    @FlowTaskBean(name = ["mergeGitlabMergeRequestTask"])
    fun mergeGitlabMergeRequestTask(
        gitlabClient: GitlabClient,
    ): MergeGitlabMergeRequestTask {
        return MergeGitlabMergeRequestTask(gitlabClient)
    }
}
