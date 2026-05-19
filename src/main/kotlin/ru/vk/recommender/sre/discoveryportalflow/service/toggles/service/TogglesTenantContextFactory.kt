package ru.vk.recommender.sre.discoveryportalflow.service.toggles.service

import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.uniqueProductionDatacenters
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.BootstrapRecomContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ShardedServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.FactorProxyServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.buildBaseReplacements
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.buildNamedReplacements
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.parseNames
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabProjectTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.context.GitlabWebhookTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabProjectInfo
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabWebhookInfo
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.client.TogglesClient
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.config.TogglesProperties
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.context.TogglesTenantTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.context.TogglesTenantType
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.util.TogglesTenantNameResolver

class TogglesTenantContextFactory(
    private val togglesProperties: TogglesProperties,
    private val togglesClient: TogglesClient,
    private val togglesTenantSupport: TogglesTenantSupport,
    private val togglesTenantRolesService: TogglesTenantRolesService,
) {

    fun buildOnline(taskRunContext: BootstrapRecomContext): TogglesTenantTaskContext {
        val tenantName = TogglesTenantNameResolver.resolveOnlineTenantName(taskRunContext.recommender.recommenderName)
        val commonContext = buildCommonContext(taskRunContext, tenantName)
        return TogglesTenantTaskContext(
            tenantType = TogglesTenantType.ONLINE,
            createTogglesTenantFlag = commonContext.createTogglesTenantFlag,
            updateZeusRolesFlag = commonContext.updateZeusRolesFlag,
            tenantName = tenantName,
            recommenderName = commonContext.recommenderName,
            owner = commonContext.owner,
            dcs = commonContext.dcs,
            abcIds = commonContext.abcIds,
            goldenSourceProjectId = requireNotNull(togglesProperties.onlineGoldenSourceTenantProjectId) {
                "toggles.online-golden-source-tenant-project-id must be configured"
            },
            replacements = buildOnlineReplacements(commonContext.recommenderName),
            gitlabProjectTaskContext = commonContext.gitlabProjectTaskContext,
            gitlabWebhookTaskContext = commonContext.gitlabWebhookTaskContext,
        )
    }

    fun buildOffline(taskRunContext: BootstrapRecomContext): TogglesTenantTaskContext {
        if (taskRunContext.services.none { it.type == ServiceType.SNAPSHOTS_BUILDER }) {
            error("Offline Toggles setup requires configured SnapshotsBuilder service")
        }

        val factorProxyConfig = taskRunContext.services
            .firstOrNull { it.type == ServiceType.FACTOR_PROXY }
            ?.requireConfig(FactorProxyServiceConfig::class) ?: error(
            "Offline Toggles setup requires configured FactorProxy service",
        )

        val factorProxyNamespace = resolveFactorProxyNamespace(taskRunContext, factorProxyConfig)
        val tenantName = TogglesTenantNameResolver.resolveOfflineTenantName(factorProxyNamespace)
        val commonContext = buildCommonContext(taskRunContext, tenantName)

        return TogglesTenantTaskContext(
            tenantType = TogglesTenantType.OFFLINE,
            createTogglesTenantFlag = commonContext.createTogglesTenantFlag,
            updateZeusRolesFlag = commonContext.updateZeusRolesFlag,
            tenantName = tenantName,
            recommenderName = commonContext.recommenderName,
            owner = commonContext.owner,
            dcs = commonContext.dcs,
            abcIds = commonContext.abcIds,
            goldenSourceProjectId = requireNotNull(togglesProperties.offlineGoldenSourceTenantProjectId) {
                "toggles.offline-golden-source-tenant-project-id must be configured"
            },
            replacements = buildOfflineReplacements(
                taskRunContext = taskRunContext,
                commonContext = commonContext,
                factorProxyNamespace = factorProxyNamespace,
                services = taskRunContext.services,
            ),
            gitlabProjectTaskContext = commonContext.gitlabProjectTaskContext,
            gitlabWebhookTaskContext = commonContext.gitlabWebhookTaskContext,
        )
    }

    private fun buildOnlineReplacements(recommenderName: String): Map<String, String> {
        return buildBaseReplacements(recommenderName) + mapOf(
            "\${Vertical}" to parseNames(recommenderName).folderName,
        )
    }

    private fun buildOfflineReplacements(
        taskRunContext: BootstrapRecomContext,
        commonContext: CommonContext,
        factorProxyNamespace: String,
        services: List<RecommenderServiceConfig>,
    ): Map<String, String> {
        val projectName = taskRunContext.recommender.projectName ?: commonContext.recommenderName
        val factorProxyDc = commonContext.dcs.firstOrNull() ?: error(
            "Offline Toggles setup requires at least one production datacenter",
        )
        val baseShardsAmount = services
            .firstOrNull { it.type == ServiceType.BASE }
            ?.let { serviceConfig -> (serviceConfig as? ShardedServiceConfig)?.shardsCount }
            ?: services
                .firstOrNull { it is ShardedServiceConfig }
                ?.let { serviceConfig -> (serviceConfig as ShardedServiceConfig).shardsCount }
            ?: 1

        return buildBaseReplacements(commonContext.recommenderName) +
            buildNamedReplacements("ProjectName", projectName) +
            mapOf(
                "\${Vertical}" to parseNames(commonContext.recommenderName).folderName,
                "\${YtMasterCluster}" to taskRunContext.ytCluster.clusterName,
                "\${ShardsAmount}" to baseShardsAmount.toString(),
                "\${FactorProxyNamespace}" to factorProxyNamespace,
                "\${FactorProxyDC}" to factorProxyDc,
            )
    }

    private fun resolveFactorProxyNamespace(
        taskRunContext: BootstrapRecomContext,
        factorProxyConfig: FactorProxyServiceConfig,
    ): String {
        return when (factorProxyConfig.serviceScope) {
            ServiceScope.PROJECT_SCOPED -> requireNotNull(taskRunContext.recommender.projectName) {
                "Project-scoped FactorProxy requires recommender.projectName"
            }

            ServiceScope.VERTICAL_SCOPED,
            ServiceScope.I2I_VERTICAL_SCOPED,
            -> taskRunContext.recommender.recommenderName
        }
    }

    private fun buildCommonContext(taskRunContext: BootstrapRecomContext, tenantName: String): CommonContext {
        val recommender = taskRunContext.recommender
        val gitlabProjectPath = togglesTenantSupport.buildGitlabProjectPath(tenantName)
        val gitlabRepositoryPath = togglesTenantSupport.buildGitlabRepositoryPath(gitlabProjectPath)
        return CommonContext(
            recommenderName = recommender.recommenderName,
            owner = recommender.serviceOwner,
            dcs = taskRunContext.dcSettings.uniqueProductionDatacenters(),
            abcIds = listOfNotNull(
                recommender.productId?.toString(),
                recommender.projectProductId?.toString(),
            ),
            createTogglesTenantFlag = !togglesClient.tenantExists(tenantName),
            updateZeusRolesFlag = togglesTenantRolesService.shouldUpdateRoles(tenantName),
            gitlabProjectTaskContext = GitlabProjectTaskContext(
                gitlabProjectInfo = GitlabProjectInfo(
                    gitlabProjectName = togglesTenantSupport.buildGitlabProjectName(tenantName),
                    gitlabProjectPath = gitlabProjectPath,
                    gitlabRepositoryPath = gitlabRepositoryPath,
                ),
            ),
            gitlabWebhookTaskContext = GitlabWebhookTaskContext(
                gitlabWebhookInfo = GitlabWebhookInfo(
                    gitlabRepositoryPath = gitlabRepositoryPath,
                    gitlabWebhookUrl = togglesTenantSupport.buildWebhookUrl(tenantName),
                ),
            ),
        )
    }

    private data class CommonContext(
        val recommenderName: String,
        val owner: String,
        val dcs: List<String>,
        val abcIds: List<String>,
        val createTogglesTenantFlag: Boolean,
        val updateZeusRolesFlag: Boolean,
        val gitlabProjectTaskContext: GitlabProjectTaskContext,
        val gitlabWebhookTaskContext: GitlabWebhookTaskContext,
    )

}
