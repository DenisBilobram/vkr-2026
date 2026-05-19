package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.common.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabProjectClient
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder.OnecloudServiceManifestBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.ServiceOneSecretOutcome
import ru.vk.recommender.sre.discoveryportalflow.service.pms.context.ServicePmsTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.pms.service.submit.GrpcProxyPmsConfigBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.pms.service.submit.ServicePmsSubmitServiceOutcome
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.service.GrpcProxyCodegenSupport
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.common.CommonService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServiceQueueSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.GrpcProxyServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.buildNamedReplacements
import java.nio.file.Path

@Component
class GrpcProxyServiceDefinition : CommonService<GrpcProxyServiceConfig>(
    type = ServiceType.GRPC_PROXY,
    serviceName = "grpc-proxy",
    sourceDirectory = "recommender/public/grpc-proxy",
    gradleBuildCommand = "recommender:public:grpc-proxy:export",
    configClass = GrpcProxyServiceConfig::class,
    sharedTemplateDirectories = emptyList(),
) {
    @Autowired
    private lateinit var grpcProxyCodegenSupport: GrpcProxyCodegenSupport

    override val defaultScope: ServiceScope = ServiceScope.PROJECT_SCOPED
    override val hasCanary: Boolean = false

    internal override fun resolveCloudServiceName(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): String {
        val grpcProxyServiceConfig = typedConfig(serviceConfig)
        return if (grpcProxyServiceConfig.existingService) {
            requireNotNull(grpcProxyServiceConfig.cloudServiceName)
        } else {
            super.resolveCloudServiceName(recommenderRuntime, serviceConfig)
        }
    }

    override fun generateCode(
        recommenderRuntime: RecommenderRuntime,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: GrpcProxyServiceConfig,
        gitlabProjectClient: GitlabProjectClient,
        serviceSecretOutcome: ServiceOneSecretOutcome?,
    ) {
        if (serviceConfig.existingService) {
            codegenSupport.renderTemplateFiles(
                templateDirectory = requireNotNull(templateDirectory),
                relativeTemplatePaths = EXISTING_SERVICE_TEMPLATE_FILES,
                templateReplacements = resolveTemplateReplacements(
                    recommenderRuntime = recommenderRuntime,
                    serviceRuntime = serviceRuntime,
                    serviceConfig = serviceConfig,
                ) + resolveOneSecretTemplateReplacements(
                    serviceRuntime = serviceRuntime,
                    serviceSecretOutcome = serviceSecretOutcome,
                ),
                gitlabProjectClient = gitlabProjectClient
            )
        } else {
            super.generateCode(
                recommenderRuntime,
                dcSettings,
                serviceRuntime,
                serviceConfig,
                gitlabProjectClient,
                serviceSecretOutcome,
            )
        }

        updateGrpcProxyCommonFiles(recommenderRuntime, serviceRuntime, gitlabProjectClient)
    }

    private fun updateGrpcProxyCommonFiles(
        recommenderRuntime: RecommenderRuntime,
        serviceRuntime: ServiceRuntime,
        gitlabProjectClient: GitlabProjectClient,
    ) {
        grpcProxyCodegenSupport.updateCommonFiles(
            recommenderRuntime = recommenderRuntime,
            serviceRuntime = serviceRuntime,
            serviceSourceDirectory = Path.of(serviceRuntime.sourceDirectory),
            gitlabProjectClient = gitlabProjectClient
        )
    }

    override fun resolveTemplateReplacements(
        recommenderRuntime: RecommenderRuntime,
        serviceRuntime: ServiceRuntime,
        serviceConfig: GrpcProxyServiceConfig,
    ): Map<String, String> {
        val projectNames = serviceRuntime.names
        val recommenderNames = recommenderRuntime.names
        val generatedGrpcHandleName = if (serviceConfig.existingService) {
            recommenderRuntime.recommenderName
        } else {
            serviceRuntime.namespace
        }
        val publicServiceOwnerClassName = if (serviceConfig.existingService) {
            recommenderNames.className
        } else {
            projectNames.className
        }
        val publicServiceName = "${publicServiceOwnerClassName}Recommender"
        val publicMethodName = "Recommend${recommenderNames.className}"

        return buildMap {
            putAll(super.resolveTemplateReplacements(recommenderRuntime, serviceRuntime, serviceConfig))
            putAll(buildNamedReplacements("RecomName", recommenderRuntime.recommenderName))
            putAll(buildNamedReplacements("ProjectName", generatedGrpcHandleName))
            put("\${ServiceSourceDirectory}", serviceRuntime.sourceDirectory)
            put("\${CloudServiceName}", serviceRuntime.cloudServiceName)
            put("\${OnecloudDirectoryName}", serviceRuntime.onecloudDirectoryName)
            put("\${RecommenderProtoRoot}", recommenderRuntime.recommenderRoot.removePrefix("recommender/").trim('/'))
            put("\${ProjectProfileEnum}", projectNames.enumName)
            put("\${ProjectProfileValue}", projectNames.packageName)
            put("\${PublicServiceName}", publicServiceName)
            put("\${PublicMethodName}", publicMethodName)
            put("\${PublicMethodJavaName}", "recommend${recommenderNames.className}")
            put("\${PumpkinMethodName}", "${publicMethodName}Pumpkin")
            put("\${PublicHandlerName}", "recommend${recommenderNames.className}Handle")
            put("\${PumpkinHandlerName}", "recommend${recommenderNames.className}PumpkinHandle")
            put("\${BackendEnumName}", "SERVICEHOST_${recommenderNames.enumName}")
            put("\${BackendValue}", "public-servicehost-${recommenderRuntime.recommenderName}")
            put("\${PublicPath}", "/api/v1/export-${recommenderRuntime.recommenderName}")
            put("\${PumpkinPath}", "/api-v1/recommend-pumpkin-${recommenderRuntime.recommenderName}")
        }
    }

    override fun resolveQueueSettings(
        recommenderRuntime: RecommenderRuntime,
        serviceConfig: RecommenderServiceConfig,
    ): ServiceQueueSettings {
        return resolveDefaultQueueSettings(recommenderRuntime, serviceConfig).copy(
            onecloudSubqueues = listOf("proxy-java"),
        )
    }

    override fun shouldGenerateOnecloudManifests(
        serviceRuntime: ServiceRuntime,
        serviceConfig: GrpcProxyServiceConfig,
    ): Boolean {
        return !serviceConfig.existingService
    }

    override fun buildOnecloudManifest(
        recommenderRuntime: RecommenderRuntime,
        createMinOneCloudConfiguration: Boolean,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: GrpcProxyServiceConfig,
    ): ServiceManifestSpec {
        val grpcProxyEnvironment = linkedMapOf(
            "STOP_TIMEOUT" to "60",
            "RSCHECK_PORT" to "81",
            "mesh_dataplane_local_dns_edit_resolv_conf" to "true",
            "mesh_dataplane_local_dns_delete_resolv_conf_options" to "rotate",
        )

        return OnecloudServiceManifestBuilder.buildServiceManifestSpec(
            productId = recommenderRuntime.projectProductId ?: recommenderRuntime.productId,
            serviceRuntime = serviceRuntime,
            defaultCloudPods = 1,
            createMinOneCloudConfiguration = createMinOneCloudConfiguration,
            cloudCpuVcores = 4,
            cloudRamG = 16,
            cloudLanOut = "100M",
            cloudLanIn = "100M",
            cloudVolumeSize = "100G",
            cloudAvailability = "85%",
            cloudJavaXms = "8g",
            cloudJavaXmx = "12g",
            additionalEnv = grpcProxyEnvironment,
            additionalTestingEnv = grpcProxyEnvironment,
            ports = listOf("80", "81", "82"),
            maxTestingCloudPods = 1,
        )
    }

    override fun submitPmsConfig(
        taskContext: ServicePmsTaskContext,
        serviceRuntime: ServiceRuntime,
        serviceConfig: GrpcProxyServiceConfig,
    ): ServicePmsSubmitServiceOutcome {
        val propertiesByEnvironment = buildGrpcProxyPmsProperties(taskContext, serviceRuntime)
        if (serviceConfig.existingService) {
            printManualPmsProperties(taskContext, serviceRuntime, propertiesByEnvironment)
            return ServicePmsSubmitServiceOutcome(apptracerConfigured = false)
        }

        val submitOutcome = super.submitPmsConfig(taskContext, serviceRuntime, serviceConfig)

        pmsSupport.submitPropertiesByEnvironment(
            serviceRuntime = serviceRuntime,
            propertiesByEnvironment = propertiesByEnvironment,
        )

        return submitOutcome
    }

    private fun buildGrpcProxyPmsProperties(
        taskContext: ServicePmsTaskContext,
        serviceRuntime: ServiceRuntime,
    ): Map<ServiceEnvironment, Map<String, String>> {
        val recommenderBackendWeightsPropertyName =
            "grpc-proxy.backend-weights-servicehost-${taskContext.recommenderName}"

        return linkedMapOf(
            ServiceEnvironment.PRODUCTION to linkedMapOf(
                "grpc-proxy.configuration" to GrpcProxyPmsConfigBuilder.buildConfiguration(
                    taskContext = taskContext,
                    serviceRuntime = serviceRuntime,
                    environment = ServiceEnvironment.PRODUCTION,
                ),
                "grpc-proxy.backend-weights" to GrpcProxyPmsConfigBuilder.buildBackendWeights(
                    taskContext = taskContext,
                    environment = ServiceEnvironment.PRODUCTION,
                ),
                recommenderBackendWeightsPropertyName to GrpcProxyPmsConfigBuilder.buildBackendWeights(
                    taskContext = taskContext,
                    environment = ServiceEnvironment.PRODUCTION,
                ),
            ),
            ServiceEnvironment.TESTING to linkedMapOf(
                "grpc-proxy.configuration" to GrpcProxyPmsConfigBuilder.buildConfiguration(
                    taskContext = taskContext,
                    serviceRuntime = serviceRuntime,
                    environment = ServiceEnvironment.TESTING,
                ),
                "grpc-proxy.backend-weights" to GrpcProxyPmsConfigBuilder.buildBackendWeights(
                    taskContext = taskContext,
                    environment = ServiceEnvironment.TESTING,
                ),
                recommenderBackendWeightsPropertyName to GrpcProxyPmsConfigBuilder.buildBackendWeights(
                    taskContext = taskContext,
                    environment = ServiceEnvironment.TESTING,
                ),
            ),
        )
    }

    private fun printManualPmsProperties(
        taskContext: ServicePmsTaskContext,
        serviceRuntime: ServiceRuntime,
        propertiesByEnvironment: Map<ServiceEnvironment, Map<String, String>>,
    ) {
        println(
            buildString {
                appendLine("Manual PMS properties for existing grpc-proxy ${serviceRuntime.cloudServiceName}:")
                appendLine("application: ${serviceRuntime.pmsApplicationName}")
                propertiesByEnvironment.forEach { (environment, properties) ->
                    if (environment != ServiceEnvironment.PRODUCTION && !serviceRuntime.supports(environment)) {
                        return@forEach
                    }

                    appendLine()
                    appendLine("environment: ${environment.name}")
                    appendLine("host: ${serviceRuntime.environment(environment).pmsHost}")
                    properties.forEach { (propertyName, propertyValue) ->
                        appendLine("property: $propertyName")
                        appendLine(
                            if (propertyName == "grpc-proxy.configuration") {
                                GrpcProxyPmsConfigBuilder.buildConfigurationPatch(
                                    taskContext = taskContext,
                                    serviceRuntime = serviceRuntime,
                                    environment = environment,
                                )
                            } else {
                                propertyValue
                            },
                        )
                    }
                }
            }.trimEnd(),
        )
    }

    private companion object {
        private val EXISTING_SERVICE_TEMPLATE_FILES = listOf(
            "\${ServiceSourceDirectory}/src/main/java/ru/vk/recommender/grpcproxy/handle/\${ProjectNamePackage}/GrpcProxy\${ProjectNameClass}Configuration.java",
            "\${ServiceSourceDirectory}/src/main/java/ru/vk/recommender/grpcproxy/handle/\${ProjectNamePackage}/\${PublicServiceName}GrpcService.java",
            "\${ServiceSourceDirectory}/src/main/proto/\${PublicServiceName}.proto",
            "\${ServiceSourceDirectory}/src/test/resources/postman/Inline\${RecomNameClass}Recommender.proto",
        )
    }
}
