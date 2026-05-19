package ru.vk.recommender.sre.discoveryportalflow.service.toggles

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskBean
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.config.GitlabProperties
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.client.DrillsClient
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.client.TogglesClient
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.config.TogglesProperties
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin.BootstrapOfflineTogglesContextTask
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin.BootstrapOnlineTogglesContextTask
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin.CreateDrillsProjectTask
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin.CreateTogglesConfigPatchTask
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin.CreateTogglesReleaseTask
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin.DeployTogglesStoredVersionTask
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin.MergeTogglesConfigPatchTask
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin.PrepareTogglesTenantRolesTask
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin.RegisterTogglesTenantTask
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin.ResolveTogglesStoredVersionTask
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.plugin.WaitTogglesConfigPatchValidationTask
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.service.TogglesGoldenSourceService
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.service.TogglesTenantContextFactory
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.service.TogglesTenantRolesService
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.service.TogglesTenantSupport

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TogglesProperties::class)
class TogglesConfiguration {

    @FlowTaskBean
    fun bootstrapOnlineTogglesContextTask(
        togglesTenantContextFactory: TogglesTenantContextFactory,
    ): BootstrapOnlineTogglesContextTask {
        return BootstrapOnlineTogglesContextTask(togglesTenantContextFactory)
    }

    @FlowTaskBean
    fun bootstrapOfflineTogglesContextTask(
        togglesTenantContextFactory: TogglesTenantContextFactory,
    ): BootstrapOfflineTogglesContextTask {
        return BootstrapOfflineTogglesContextTask(togglesTenantContextFactory)
    }

    @FlowTaskBean(name = ["createDrillsProjectTask"])
    fun createDrillsProjectTask(
        drillsClient: DrillsClient,
    ): CreateDrillsProjectTask {
        return CreateDrillsProjectTask(drillsClient)
    }

    @FlowTaskBean(name = ["registerTogglesTenantTask"])
    fun registerTogglesTenantTask(
        togglesClient: TogglesClient,
    ): RegisterTogglesTenantTask {
        return RegisterTogglesTenantTask(togglesClient)
    }

    @FlowTaskBean(name = ["createTogglesConfigPatchTask"])
    fun createTogglesConfigPatchTask(
        togglesClient: TogglesClient,
        togglesGoldenSourceService: TogglesGoldenSourceService,
    ): CreateTogglesConfigPatchTask {
        return CreateTogglesConfigPatchTask(togglesClient, togglesGoldenSourceService)
    }

    @FlowTaskBean(name = ["waitTogglesConfigPatchValidationTask"])
    fun waitTogglesConfigPatchValidationTask(
        togglesClient: TogglesClient,
    ): WaitTogglesConfigPatchValidationTask {
        return WaitTogglesConfigPatchValidationTask(togglesClient)
    }

    @FlowTaskBean(name = ["mergeTogglesConfigPatchTask"])
    fun mergeTogglesConfigPatchTask(
        togglesClient: TogglesClient,
    ): MergeTogglesConfigPatchTask {
        return MergeTogglesConfigPatchTask(togglesClient)
    }

    @FlowTaskBean(name = ["createTogglesReleaseTask"])
    fun createTogglesReleaseTask(
        togglesClient: TogglesClient,
    ): CreateTogglesReleaseTask {
        return CreateTogglesReleaseTask(togglesClient)
    }

    @FlowTaskBean(name = ["resolveTogglesStoredVersionTask"])
    fun resolveTogglesStoredVersionTask(
        togglesClient: TogglesClient,
    ): ResolveTogglesStoredVersionTask {
        return ResolveTogglesStoredVersionTask(togglesClient)
    }

    @FlowTaskBean(name = ["deployTogglesStoredVersionTask"])
    fun deployTogglesStoredVersionTask(
        togglesClient: TogglesClient,
        togglesTenantSupport: TogglesTenantSupport,
    ): DeployTogglesStoredVersionTask {
        return DeployTogglesStoredVersionTask(togglesClient, togglesTenantSupport)
    }

    @FlowTaskBean(name = ["prepareTogglesTenantRolesTask"])
    fun prepareTogglesTenantRolesTask(
        togglesTenantRolesService: TogglesTenantRolesService,
    ): PrepareTogglesTenantRolesTask {
        return PrepareTogglesTenantRolesTask(togglesTenantRolesService)
    }

    @Bean
    fun togglesTenantContextFactory(
        togglesProperties: TogglesProperties,
        togglesClient: TogglesClient,
        togglesTenantSupport: TogglesTenantSupport,
        togglesTenantRolesService: TogglesTenantRolesService,
    ): TogglesTenantContextFactory {
        return TogglesTenantContextFactory(
            togglesProperties,
            togglesClient,
            togglesTenantSupport,
            togglesTenantRolesService,
        )
    }

    @Bean
    fun togglesGoldenSourceService(
        gitlabClient: GitlabClient,
    ): TogglesGoldenSourceService {
        return TogglesGoldenSourceService(gitlabClient)
    }

    @Bean
    fun togglesTenantSupport(
        gitlabProperties: GitlabProperties,
    ): TogglesTenantSupport {
        return TogglesTenantSupport(gitlabProperties)
    }

    @Bean
    fun togglesTenantRolesService(
        togglesProperties: TogglesProperties,
        gitlabClient: GitlabClient,
    ): TogglesTenantRolesService {
        return TogglesTenantRolesService(togglesProperties, gitlabClient)
    }

    @Bean
    fun togglesClient(
        togglesProperties: TogglesProperties,
        gitlabProperties: GitlabProperties,
    ): TogglesClient {
        return TogglesClient(togglesProperties, gitlabProperties)
    }

    @Bean
    fun drillsClient(): DrillsClient {
        return DrillsClient()
    }
}
