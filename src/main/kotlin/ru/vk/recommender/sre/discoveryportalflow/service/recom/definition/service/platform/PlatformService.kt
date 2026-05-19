package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.platform

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabProjectClient
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder.OnecloudServiceManifestBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.ServiceOneSecretOutcome
import ru.vk.recommender.sre.discoveryportalflow.service.pms.writer.PmsConfpFileNameMode
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServicePmsDefinition
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.PlatformServiceNaming
import kotlin.reflect.KClass

abstract class PlatformService<TConfig : RecommenderServiceConfig>(
    type: ServiceType,
    serviceName: String,
    configClass: KClass<TConfig>,
    templateDirectory: String = "platform-$serviceName",
    sharedTemplateDirectories: List<String> = listOf("shared/platform"),
    sourceDirectory: String = PlatformServiceNaming.sourceDirectory(serviceName),
    gradleBuildCommand: String = PlatformServiceNaming.gradleBuildCommand(serviceName),
    onecloudDirectoryName: String = PlatformServiceNaming.onecloudDirectoryName(serviceName),
    defaultScope: ServiceScope = ServiceScope.VERTICAL_SCOPED,
    hasSecret: Boolean = false,
    allowEmptySecretData: Boolean = false,
    hasTesting: Boolean = true,
    hasCanary: Boolean = true,
    val udfModuleName: String = PlatformServiceNaming.udfModuleName(serviceName),
) : RecomService<TConfig>(
    type = type,
    serviceName = serviceName,
    configClass = configClass,
    templateDirectory = templateDirectory,
    sharedTemplateDirectories = sharedTemplateDirectories,
    sourceDirectory = sourceDirectory,
    gradleBuildCommand = gradleBuildCommand,
    onecloudDirectoryName = onecloudDirectoryName,
    defaultScope = defaultScope,
    hasSecret = hasSecret,
    allowEmptySecretData = allowEmptySecretData,
    hasTesting = hasTesting,
    hasCanary = hasCanary,
) {
    override val pms: ServicePmsDefinition =
        ServicePmsDefinition(
            confpFileNameMode = PmsConfpFileNameMode.TENANT_SCOPED,
        )

    override fun generateCode(
        recommenderRuntime: RecommenderRuntime,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: TConfig,
        gitlabProjectClient: GitlabProjectClient,
        serviceSecretOutcome: ServiceOneSecretOutcome?,
    ) {
        val udfSourceDirectory = PlatformServiceNaming.udfSourceDirectory(
            recommenderRuntime = recommenderRuntime,
            udfModuleName = udfModuleName,
        )
        codegenSupport.addPlatformUdfModule(
            serviceRuntime = serviceRuntime,
            udfSourceDirectory = udfSourceDirectory,
            gitlabProjectClient = gitlabProjectClient
        )
        codegenSupport.renderTemplateDirectories(
            templateDirectories = resolveGeneratedTemplateDirectories(),
            templateReplacements = resolveTemplateReplacements(
                recommenderRuntime = recommenderRuntime,
                serviceRuntime = serviceRuntime,
                serviceConfig = serviceConfig,
            ) + mapOf(
                "\${UdfSourceDirectory}" to udfSourceDirectory,
                "\${UdfModuleName}" to udfModuleName,
                "\${PlatformServicePackageSegment}" to PlatformServiceNaming.packageSegment(serviceName),
            ) + resolveOneSecretTemplateReplacements(
                serviceRuntime = serviceRuntime,
                serviceSecretOutcome = serviceSecretOutcome,
            ),
            gitlabProjectClient = gitlabProjectClient
        )

        codegenSupport.writeServiceInfo(
            recommenderRuntime,
            dcSettings,
            serviceRuntime,
            isPlatformService = true,
            gitlabProjectClient
        )
    }

    override fun buildOnecloudManifest(
        recommenderRuntime: RecommenderRuntime,
        createMinOneCloudConfiguration: Boolean,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        serviceConfig: TConfig,
    ): ServiceManifestSpec {
        val platformEnvironment = OnecloudServiceManifestBuilder.buildPlatformEnvironment(serviceRuntime)
        val zenApplicationName = serviceRuntime.onecloudDirectoryName
        return OnecloudServiceManifestBuilder.buildServiceManifestSpec(
            productId = recommenderRuntime.productId,
            serviceRuntime = serviceRuntime,
            defaultCloudPods = 5,
            createMinOneCloudConfiguration = createMinOneCloudConfiguration,
            cloudCpuVcores = 8,
            cloudRamG = 16,
            cloudLanOut = "300M",
            cloudLanIn = "400M",
            cloudVolumeSize = "200g",
            cloudAvailability = "80%",
            cloudJavaXms = "8g",
            cloudJavaXmx = "8g",
            additionalEnv = platformEnvironment,
            additionalTestingEnv = platformEnvironment,
            extraPorts = listOf("40195"),
            maxTestingCloudPods = 1,
            zenApplicationName = zenApplicationName
        )
    }
}
