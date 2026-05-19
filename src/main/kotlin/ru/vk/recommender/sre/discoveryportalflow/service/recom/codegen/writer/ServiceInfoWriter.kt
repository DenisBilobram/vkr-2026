package ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.writer

import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.util.AppTracerProjectNameResolver
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabProjectClient
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import java.nio.file.Path

class ServiceInfoWriter(
    private val templateTreeRenderer: ServiceTemplateTreeRenderer,
) {

    fun writeServiceInfo(
        recommenderRuntime: RecommenderRuntime,
        serviceRuntime: ServiceRuntime,
        dcSettings: RecommenderDcSettings,
        isPlatformService: Boolean,
        gitlabProjectClient: GitlabProjectClient
    ) {
        val serviceInfoPaths = resolveServiceInfoPaths(serviceRuntime.cloudServiceName)
        val serviceInfoTemplateValues = buildServiceInfoTemplateValues(
            recommenderRuntime = recommenderRuntime,
            serviceRuntime = serviceRuntime,
            dcSettings = dcSettings,
            isPlatformService = isPlatformService,
        )
        templateTreeRenderer.renderTemplateDirectories(
            templateDirectories = listOf(SERVICE_INFO_TEMPLATE_DIRECTORY),
            templateReplacements = serviceInfoTemplateValues,
            gitlabProjectClient = gitlabProjectClient
        )
        appendServiceInfoPathIfMissing(
            servicesDirectory = serviceInfoPaths.servicesDirectory,
            relativeServiceInfoPath = serviceInfoPaths.relativeServiceInfoPath,
            gitlabProjectClient = gitlabProjectClient
        )
    }

    private fun resolveServiceInfoPaths(
        cloudServiceName: String,
    ): ServiceInfoPaths {
        val servicesDirectory = Path.of("scripts3/services")
        val relativeServiceInfoPath = "./info/$cloudServiceName-service-info.yaml"

        return ServiceInfoPaths(
            servicesDirectory = servicesDirectory,
            relativeServiceInfoPath = relativeServiceInfoPath,
        )
    }

    private fun buildServiceInfoTemplateValues(
        recommenderRuntime: RecommenderRuntime,
        serviceRuntime: ServiceRuntime,
        dcSettings: RecommenderDcSettings,
        isPlatformService: Boolean,
    ): Map<String, String> {
        val ownerLogins = buildOwnerLogins(
            ownerLogin = recommenderRuntime.serviceOwner,
            additionalResponsibleLogins = recommenderRuntime.additionalResponsibles,
        )
        val followerLogins = buildFollowerLogins(
            ownerLogin = recommenderRuntime.serviceOwner,
            additionalResponsibleLogins = recommenderRuntime.additionalResponsibles,
            additionalFollowerLogins = recommenderRuntime.additionalFollowers,
        )

        val serviceImageConfiguration = resolveServiceImageConfiguration(serviceRuntime, isPlatformService)
        val optionalBlocks = resolveOptionalTemplateBlocks(serviceRuntime, isPlatformService)
        return mapOf(
            placeholder("CloudServiceName") to serviceRuntime.cloudServiceName,
            placeholder("ServiceInfoOwner") to recommenderRuntime.serviceOwner,
            placeholder("GradleBuildCommand") to serviceRuntime.gradleBuildCommand,
            placeholder("ServiceSourceDirectory") to serviceRuntime.sourceDirectory,
            placeholder("TeamcityProject") to serviceRuntime.teamcityProject,
            placeholder("GradleTaskParamBlock") to optionalBlocks.gradleTaskParamBlock,
            placeholder("PlatformServiceNameBlock") to optionalBlocks.platformServiceNameBlock,
            placeholder("ZAdminPortBlock") to optionalBlocks.zAdminPortBlock,
            placeholder("AlertsBlock") to optionalBlocks.alertsBlock,
            placeholder("DockerImageName") to serviceImageConfiguration.applicationName,
            placeholder("ReleaseOptionsName") to serviceImageConfiguration.releaseOptionsName,
            placeholder("VersionPrefix") to serviceImageConfiguration.versionPrefix,
            placeholder("TagPrefix") to serviceImageConfiguration.tagPrefix,
            placeholder("ReleaseComponent") to serviceImageConfiguration.releaseComponent,
            placeholder("TicketComponent") to serviceImageConfiguration.ticketComponent,
            placeholder("FollowersBlock") to buildYamlListBlock(
                keyName = "followers",
                values = followerLogins,
                keyIndentSize = 6,
            ),
            placeholder("OwnersBlock") to buildYamlListBlock(
                keyName = "owners",
                values = ownerLogins,
                keyIndentSize = 4,
            ),
            placeholder("IsUnifiedImage") to "false",
            placeholder("NeedFormulasRelease") to "false",
            placeholder("ApplicationServiceName") to serviceRuntime.cloudServiceName,
            placeholder("TracerProjectName") to AppTracerProjectNameResolver.resolveProjectName(recommenderRuntime.recommenderName),
            placeholder("ProductionLinks") to buildOnecloudLinksByEnvironment(
                serviceRuntime = serviceRuntime,
                environment = ServiceEnvironment.PRODUCTION,
                datacenterCodes = dcSettings.productionDcs,
            ),
            placeholder("CanaryLinks") to buildOnecloudLinksByEnvironment(
                serviceRuntime = serviceRuntime,
                environment = ServiceEnvironment.CANARY,
                datacenterCodes = dcSettings.canaryDcs,
            ),
            placeholder("TestingLinks") to buildOnecloudLinksByEnvironment(
                serviceRuntime = serviceRuntime,
                environment = ServiceEnvironment.TESTING,
                datacenterCodes = dcSettings.testingDcs,
            ),
        )
    }

    private fun resolveServiceImageConfiguration(
        serviceRuntime: ServiceRuntime,
        isPlatformService: Boolean,
    ): ServiceImageConfiguration {
        val dockerImageName = serviceRuntime.cloudServiceName
        val tagPrefix = if (isPlatformService) {
            "$dockerImageName-release"
        } else {
            serviceRuntime.cloudServiceName
        }

        return ServiceImageConfiguration(
            releaseOptionsName = dockerImageName,
            versionPrefix = "${dockerImageName}-release-",
            tagPrefix = tagPrefix,
            releaseComponent = "${dockerImageName}_release",
            ticketComponent = dockerImageName,
            applicationName = dockerImageName,
        )
    }

    private fun resolveOptionalTemplateBlocks(
        serviceRuntime: ServiceRuntime,
        isPlatformService: Boolean,
    ): ServiceInfoOptionalBlocks {
        val gradleTaskParam = if (isPlatformService) {
                "platform.product=${serviceRuntime.namespace}"
            } else {
                null
            }

        val platformServiceName = if (isPlatformService) {
                serviceRuntime.onecloudDirectoryName
            } else {
                null
            }

        return ServiceInfoOptionalBlocks(
            gradleTaskParamBlock = gradleTaskParam?.let { "    gradle_task_param: $it\n" } ?: "",
            platformServiceNameBlock = platformServiceName?.let { "    platform_service_name: $it\n" } ?: "",
            zAdminPortBlock = "",
            alertsBlock = "",
        )
    }

    private fun buildOnecloudLinksByEnvironment(
        serviceRuntime: ServiceRuntime,
        environment: ServiceEnvironment,
        datacenterCodes: List<String>,
    ): String {
        if (environment != ServiceEnvironment.PRODUCTION && !serviceRuntime.supports(environment)) {
            return ""
        }

        val environmentRuntime = serviceRuntime.environment(environment)
        val onecloudSubqueues = serviceRuntime.distinctOnecloudSubqueues()
        return buildString {
            appendLine("      ${environment.id}:")
            onecloudSubqueues.forEach { shardQueueName ->
                datacenterCodes.forEach { datacenterCode ->
                    appendLine(
                        "        - https://cloud.nda.example.invalid/cloud/${datacenterCode.uppercase()}/ns/public/service/$shardQueueName.${environmentRuntime.cloudServiceName}",
                    )
                }
            }
        }
    }

    private fun buildOwnerLogins(
        ownerLogin: String,
        additionalResponsibleLogins: List<String>,
    ): List<String> {
        return buildList {
            add(ownerLogin)
            addAll(additionalResponsibleLogins)
        }
    }

    private fun buildFollowerLogins(
        ownerLogin: String,
        additionalResponsibleLogins: List<String>,
        additionalFollowerLogins: List<String>,
    ): List<String> {
        return buildList {
            add(ownerLogin)
            addAll(additionalResponsibleLogins)
            addAll(additionalFollowerLogins)
        }
    }

    private fun buildYamlListBlock(
        keyName: String,
        values: List<String>,
        keyIndentSize: Int,
    ): String {
        val keyIndent = " ".repeat(keyIndentSize)
        if (values.isEmpty()) {
            return "$keyIndent$keyName: []\n"
        }

        val itemIndent = " ".repeat(keyIndentSize + 2)
        return buildString {
            appendLine("$keyIndent$keyName:")
            values.forEach { value ->
                appendLine("$itemIndent- \"${escapeYamlString(value)}\"")
            }
        }
    }

    private fun appendServiceInfoPathIfMissing(
        servicesDirectory: Path,
        relativeServiceInfoPath: String,
        gitlabProjectClient: GitlabProjectClient
    ) {
        val serviceInfoPathsFilePath = servicesDirectory.resolve("services-info-paths.yaml")
        val file = gitlabProjectClient.getFileOptional(serviceInfoPathsFilePath)
        if (file.content == null) {
            file.content = buildDefaultServiceInfoPathsContent(relativeServiceInfoPath)
            return
        }

        val serviceInfoPathsLines = file.content!!.split("\n").toMutableList()
        if (serviceInfoPathsLines.any { line -> line.trim() == "- $relativeServiceInfoPath" }) {
            return
        }

        val targetsLineIndex = serviceInfoPathsLines.indexOfFirst { line -> line.trim() == "targets:" }
        if (targetsLineIndex == -1) {
            file.content = buildServiceInfoPathsContentWithAppendedTarget(
                existingLines = serviceInfoPathsLines,
                relativeServiceInfoPath = relativeServiceInfoPath,
            )
            return
        }

        val newTargetLine = "  - $relativeServiceInfoPath"
        val insertionIndex = findTargetsInsertionIndex(serviceInfoPathsLines, targetsLineIndex)
        serviceInfoPathsLines.add(insertionIndex, newTargetLine)

        file.content = serviceInfoPathsLines.joinToString(separator = "\n", postfix = "\n")
        return
    }

    private fun findTargetsInsertionIndex(
        serviceInfoPathsLines: List<String>,
        targetsLineIndex: Int,
    ): Int {
        var insertionIndex = targetsLineIndex + 1
        while (insertionIndex < serviceInfoPathsLines.size && serviceInfoPathsLines[insertionIndex].trim().startsWith("- ")) {
            insertionIndex += 1
        }
        return insertionIndex
    }

    private fun buildServiceInfoPathsContentWithAppendedTarget(
        existingLines: List<String>,
        relativeServiceInfoPath: String,
    ): String {
        return buildString {
            append(existingLines.joinToString(separator = "\n"))
            if (existingLines.isNotEmpty() && existingLines.last().isNotBlank()) {
                append('\n')
            }
            appendLine("spec:")
            appendLine("  targets:")
            appendLine("  - $relativeServiceInfoPath")
        }
    }

    private fun buildDefaultServiceInfoPathsContent(relativeServiceInfoPath: String): String {
        return buildString {
            appendLine("apiVersion: backstage.io/v1alpha1")
            appendLine("kind: Location")
            appendLine("metadata:")
            appendLine("  name: services-info-paths")
            appendLine("spec:")
            appendLine("  type: file")
            appendLine("  targets:")
            appendLine("  - $relativeServiceInfoPath")
        }
    }

    private fun escapeYamlString(rawValue: String): String {
        return rawValue
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }

    private fun placeholder(name: String): String {
        return "\${$name}"
    }

    private data class ServiceInfoPaths(
        val servicesDirectory: Path,
        val relativeServiceInfoPath: String,
    )

    private data class ServiceImageConfiguration(
        val releaseOptionsName: String,
        val versionPrefix: String,
        val tagPrefix: String,
        val releaseComponent: String,
        val ticketComponent: String,
        val applicationName: String,
    )

    private data class ServiceInfoOptionalBlocks(
        val gradleTaskParamBlock: String,
        val platformServiceNameBlock: String,
        val zAdminPortBlock: String,
        val alertsBlock: String,
    )

    private companion object {
        private const val SERVICE_INFO_TEMPLATE_DIRECTORY = "shared/service-info"
        private const val DEFAULT_TRACER_PROJECT_NAME = "recommender"
    }
}
