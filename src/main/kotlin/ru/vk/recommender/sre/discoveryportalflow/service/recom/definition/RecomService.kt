package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition

import org.springframework.beans.factory.annotation.Autowired
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabProjectClient
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.ServiceOneSecretOutcome
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.util.OneSecretPathBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.pms.context.ServicePmsTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.pms.service.ServicePmsSupport
import ru.vk.recommender.sre.discoveryportalflow.service.pms.service.submit.ServicePmsSubmitServiceOutcome
import ru.vk.recommender.sre.discoveryportalflow.service.pms.writer.PmsConfpFileNameMode
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.service.ServiceCodegenSupport
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.support.Log4jTemplateSupport
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.capability.ServiceCodegenCapability
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.capability.ServiceOneSecretCapability
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.capability.ServiceOnecloudCapability
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.capability.ServicePmsCapability
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.capability.ServiceTeamcityCapability
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ScopedServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServicePmsDefinition
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServiceQueueNames
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServiceQueueSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironmentRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.buildBaseReplacements
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.parseNames
import kotlin.reflect.KClass

abstract class RecomService<TConfig : RecommenderServiceConfig>(
    val type: ServiceType,
    val serviceName: String,
    val configClass: KClass<TConfig>,
    override val templateDirectory: String? = serviceName,
    override val sharedTemplateDirectories: List<String> = emptyList(),
    override val sourceDirectory: String? = null,
    override val gradleBuildCommand: String? = null,
    override val onecloudDirectoryName: String? = null,
    open val defaultScope: ServiceScope = ServiceScope.VERTICAL_SCOPED,
    override val pms: ServicePmsDefinition = ServicePmsDefinition(),
    override val hasSecret: Boolean = false,
    override val allowEmptySecretData: Boolean = false,
    override val hasTesting: Boolean = true,
    override val hasCanary: Boolean = true,
) : ServiceCodegenCapability<TConfig>,
    ServiceOnecloudCapability<TConfig>,
    ServicePmsCapability<TConfig>,
    ServiceOneSecretCapability<TConfig>,
    ServiceTeamcityCapability<TConfig> {
    @Autowired
    protected lateinit var codegenSupport: ServiceCodegenSupport

    @Autowired
    protected lateinit var pmsSupport: ServicePmsSupport

    abstract override fun generateCode(
        recommenderRuntime: RecommenderRuntime,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: TConfig,
        gitlabProjectClient: GitlabProjectClient,
        serviceSecretOutcome: ServiceOneSecretOutcome?,
    )

    abstract override fun buildOnecloudManifest(
        recommenderRuntime: RecommenderRuntime,
        createMinOneCloudConfiguration: Boolean,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: TConfig,
    ): ServiceManifestSpec

    internal open fun resolveServiceScope(serviceConfig: RecommenderServiceConfig): ServiceScope {
        return (serviceConfig as? ScopedServiceConfig)?.serviceScope
            ?: defaultScope
    }

    internal fun resolveNamespaceName(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): String {
        return when (resolveServiceScope(serviceConfig)) {
            ServiceScope.PROJECT_SCOPED -> requireNotNull(recommenderRuntime.projectName) {
                "Project-scoped service '$serviceName' requires recommender.projectName"
            }

            ServiceScope.VERTICAL_SCOPED, ServiceScope.I2I_VERTICAL_SCOPED -> recommenderRuntime.recommenderName
        }
    }

    internal open fun resolveCloudServiceName(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): String {
        return "${resolveNamespaceName(recommenderRuntime, serviceConfig)}-$serviceName"
    }

    override fun submitPmsConfig(
        taskContext: ServicePmsTaskContext,
        serviceRuntime: ServiceRuntime,
        serviceConfig: TConfig,
    ): ServicePmsSubmitServiceOutcome {
        if (pms.submitApptracer) {
            pmsSupport.submitApptracerProperties(taskContext, serviceRuntime)
            return ServicePmsSubmitServiceOutcome(true)
        }
        else return ServicePmsSubmitServiceOutcome(false)
    }

    override fun resolveSourceDirectory(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): String {
        sourceDirectory?.let { return it }
        return resolveScopedSourceDirectory(
            recommenderRuntime = recommenderRuntime,
            serviceScope = resolveServiceScope(serviceConfig),
            scopedServiceName = serviceName,
        )
    }

    override fun resolveGradleBuildCommand(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): String {
        gradleBuildCommand?.let { return it }
        return resolveScopedGradleBuildCommand(
            recommenderRuntime = recommenderRuntime,
            serviceScope = resolveServiceScope(serviceConfig),
            scopedServiceName = serviceName,
        )
    }

    override fun resolveTeamcityProject(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): String {
        return resolveScopedTeamcityProject(
            recommenderRuntime = recommenderRuntime,
            serviceScope = resolveServiceScope(serviceConfig),
            serviceClassName = parseNames(serviceName).className,
        )
    }

    override fun resolveQueueSettings(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): ServiceQueueSettings {
        return resolveDefaultQueueSettings(recommenderRuntime, serviceConfig)
    }

    override fun resolveOnecloudDirectoryName(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): String {
        onecloudDirectoryName?.let { return it }
        return resolveCloudServiceName(recommenderRuntime, serviceConfig)
    }

    override fun resolveTemplateReplacements(
        recommenderRuntime: RecommenderRuntime,
        serviceRuntime: ServiceRuntime,
        serviceConfig: TConfig,
    ): Map<String, String> {
        return buildMap {
            putAll(buildBaseReplacements(serviceRuntime.namespace))
            put("\${Vertical}", recommenderRuntime.vertical)
            put("\${Namespace}", serviceRuntime.namespace)
            put("\${DictionaryBaseProject}", recommenderRuntime.dictionaryBaseProject)
            put("\${ServiceSourceDirectory}", serviceRuntime.sourceDirectory)
            put("\${CloudServiceName}", serviceRuntime.cloudServiceName)
            put("\${OnecloudDirectoryName}", serviceRuntime.onecloudDirectoryName)
            put("\${PmsApplicationName}", serviceRuntime.pmsApplicationName)
            put("\${ConfpFileName}", resolveConfpFileName(serviceRuntime))
            put("\${ConfpExtraBackends}", "")
            put("\${DockerfileDragonflyFragment}", "")
            put("\${Log4jPackages}", Log4jTemplateSupport.DEFAULT_PACKAGES)
            put("\${Log4jDebugAppenderBlock}", Log4jTemplateSupport.disabledDebugAppenderBlock())
            put("\${Log4jPerfMetricsAppenderBlock}", Log4jTemplateSupport.disabledPerfMetricsAppenderBlock())
            put("\${Log4jRuntimeMetricsAppenderBlock}", Log4jTemplateSupport.disabledRuntimeMetricsAppenderBlock())
            put("\${Log4jRecommendsAppenderBlock}", Log4jTemplateSupport.disabledRecommendsAppenderBlock())
            put("\${Log4jFeaturesAppenderBlock}", Log4jTemplateSupport.disabledFeaturesAppenderBlock())
            put("\${Log4jFunnelAppenderBlock}", Log4jTemplateSupport.disabledFunnelAppenderBlock())
            put("\${Log4jRecommendsLoggerBlock}", Log4jTemplateSupport.disabledRecommendsLoggerBlock())
            put("\${Log4jFeaturesLoggerBlock}", Log4jTemplateSupport.disabledFeaturesLoggerBlock())
            put("\${Log4jFunnelLoggerBlock}", Log4jTemplateSupport.disabledFunnelLoggerBlock())
            put("\${Log4jRuntimeMetricsLoggerBlock}", Log4jTemplateSupport.disabledRuntimeMetricsLoggerBlock())
            put("\${Log4jPerfMetricsLoggerBlock}", Log4jTemplateSupport.disabledPerfMetricsLoggerBlock())
        }
    }

    override fun resolveGeneratedTemplateDirectories(): List<String> {
        return buildList {
            addAll(sharedTemplateDirectories)
            templateDirectory?.let { add(it) }
        }
    }

    protected fun resolveDefaultQueueSettings(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): ServiceQueueSettings {
        return ServiceQueueSettings(
            rootQueueNames = resolveScopedRootQueueNames(
                recommenderRuntime = recommenderRuntime,
                serviceScope = resolveServiceScope(serviceConfig),
            ),
        )
    }

    protected fun resolveScopedSourceDirectory(
        recommenderRuntime: RecommenderRuntime,
        serviceScope: ServiceScope,
        scopedServiceName: String,
    ): String {
        return if (serviceScope == ServiceScope.PROJECT_SCOPED) {
            "${recommenderRuntime.recommenderRoot}$scopedServiceName"
        } else {
            "${recommenderRuntime.recommenderRoot}${recommenderRuntime.recommenderName}/$scopedServiceName"
        }
    }

    protected fun resolveScopedGradleBuildCommand(
        recommenderRuntime: RecommenderRuntime,
        serviceScope: ServiceScope,
        scopedServiceName: String,
    ): String {
        val recommenderRootPath = recommenderRuntime.recommenderRoot.replace('/', ':')
        return if (serviceScope == ServiceScope.PROJECT_SCOPED) {
            "$recommenderRootPath$scopedServiceName:export"
        } else {
            "$recommenderRootPath${recommenderRuntime.recommenderName}:$scopedServiceName:export"
        }
    }

    protected fun resolveScopedTeamcityProject(
        recommenderRuntime: RecommenderRuntime,
        serviceScope: ServiceScope,
        serviceClassName: String,
    ): String {
        return when (serviceScope) {
            ServiceScope.PROJECT_SCOPED -> "${recommenderRuntime.teamcityProjectPrefix}_$serviceClassName"
            ServiceScope.VERTICAL_SCOPED, ServiceScope.I2I_VERTICAL_SCOPED -> "${recommenderRuntime.teamcityProjectPrefix}_${recommenderRuntime.names.className}_$serviceClassName"
        }
    }

    protected fun resolveScopedRootQueueNames(
        recommenderRuntime: RecommenderRuntime,
        serviceScope: ServiceScope,
    ): ServiceQueueNames {
        return recommenderRuntime.resolveRootQueueNames(serviceScope)
    }

    protected fun resolveConfpFileName(serviceRuntime: ServiceRuntime): String {
        return when (pms.confpFileNameMode) {
            PmsConfpFileNameMode.DEFAULT -> "confp.yml"
            PmsConfpFileNameMode.TENANT_SCOPED -> "confp-${serviceRuntime.namespace}.yml"
        }
    }

    protected fun resolveOneSecretTemplateReplacements(
        serviceRuntime: ServiceRuntime,
        serviceSecretOutcome: ServiceOneSecretOutcome?,
    ): Map<String, String> {
        return mapOf(
            "\${OneSecretProductionServiceSecretEntry}" to buildOneSecretServiceSecretEntry(
                serviceRuntime = serviceRuntime,
                environment = ServiceEnvironment.PRODUCTION,
                serviceSecretOutcome = serviceSecretOutcome,
            ),
            "\${OneSecretCanaryServiceSecretEntry}" to buildOneSecretServiceSecretEntry(
                serviceRuntime = serviceRuntime,
                environment = ServiceEnvironment.CANARY,
                serviceSecretOutcome = serviceSecretOutcome,
            ),
            "\${OneSecretTestingServiceSecretEntry}" to buildOneSecretServiceSecretEntry(
                serviceRuntime = serviceRuntime,
                environment = ServiceEnvironment.TESTING,
                serviceSecretOutcome = serviceSecretOutcome,
            ),
        )
    }

    private fun buildOneSecretServiceSecretEntry(
        serviceRuntime: ServiceRuntime,
        environment: ServiceEnvironment,
        serviceSecretOutcome: ServiceOneSecretOutcome?,
    ): String {
        serviceSecretOutcome ?: return ""

        val environmentRuntime = serviceRuntime.environment(environment)
        val secretId = serviceSecretOutcome.secretIdFor(environment)?.takeIf { it.isNotBlank() } ?: return ""
        if (!shouldRenderOneSecretServiceSecretEntry(environment, environmentRuntime, serviceRuntime)) {
            return ""
        }

        val secretPath = OneSecretPathBuilder.build(environmentRuntime, secretId.trim())
        return buildString {
            appendLine("  - vault: $secretPath")
            appendLine("    path: ${environmentRuntime.applicationSecretPath}")
            append("    format: java")
        }
    }

    private fun shouldRenderOneSecretServiceSecretEntry(
        environment: ServiceEnvironment,
        environmentRuntime: ServiceEnvironmentRuntime,
        serviceRuntime: ServiceRuntime,
    ): Boolean {
        if (environmentRuntime.rootQueueName.isBlank()) {
            return false
        }

        return when (environment) {
            ServiceEnvironment.PRODUCTION -> true
            ServiceEnvironment.CANARY,
            ServiceEnvironment.TESTING -> serviceRuntime.supports(environment)
        }
    }

    override fun buildSecretPairs(
        taskContext: OneSecretTaskContext,
        serviceRuntime: ServiceRuntime,
        serviceConfig: TConfig,
    ): Map<String, String> {
        return emptyMap()
    }

    protected fun typedConfig(serviceConfig: RecommenderServiceConfig): TConfig {
        return serviceConfig.requireConfig(configClass)
    }
}
