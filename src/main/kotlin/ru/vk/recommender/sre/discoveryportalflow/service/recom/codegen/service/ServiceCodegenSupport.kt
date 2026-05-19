package ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.service

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabProjectClient
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.generator.ProtoGenerator
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.writer.ServiceInfoWriter
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.writer.ServiceTemplateTreeRenderer
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import java.nio.file.Path

class ServiceCodegenSupport(
    private val protoGenerator: ProtoGenerator,
    private val templateTreeRenderer: ServiceTemplateTreeRenderer,
    private val serviceInfoWriter: ServiceInfoWriter,
) {

    fun patchMetaConfig(recommenderRuntime: RecommenderRuntime, gitlabProjectClient: GitlabProjectClient) {
        protoGenerator.insertMetaRecommenderConfig(recommenderRuntime, gitlabProjectClient)
    }

    fun addProjectModule(
        serviceRuntime: ServiceRuntime,
        gitlabProjectClient: GitlabProjectClient,
    ) {
        protoGenerator.addProjectModule(
            sourceDirectory = serviceRuntime.sourceDirectory,
            gradleBuildCommand = serviceRuntime.gradleBuildCommand,
            gitlabProjectClient = gitlabProjectClient
        )
    }

    fun renderTemplateDirectories(
        templateDirectories: List<String>,
        templateReplacements: Map<String, String>,
        gitlabProjectClient: GitlabProjectClient,
    ) {
        templateTreeRenderer.renderTemplateDirectories(
            templateDirectories = templateDirectories,
            templateReplacements = templateReplacements,
            gitlabProjectClient = gitlabProjectClient
        )
    }

    fun renderTemplateFiles(
        templateDirectory: String,
        relativeTemplatePaths: List<String>,
        templateReplacements: Map<String, String>,
        gitlabProjectClient: GitlabProjectClient,
    ) {
        templateTreeRenderer.renderTemplateFiles(
            templateDirectory = templateDirectory,
            relativeTemplatePaths = relativeTemplatePaths,
            templateReplacements = templateReplacements,
            gitlabProjectClient = gitlabProjectClient,
        )
    }

    fun writeServiceInfo(
        recommenderRuntime: RecommenderRuntime,
        dcSettings: RecommenderDcSettings,
        serviceRuntime: ServiceRuntime,
        isPlatformService: Boolean,
        gitlabProjectClient: GitlabProjectClient
    ) {
        serviceInfoWriter.writeServiceInfo(
            recommenderRuntime = recommenderRuntime,
            serviceRuntime = serviceRuntime,
            dcSettings = dcSettings,
            isPlatformService = isPlatformService,
            gitlabProjectClient = gitlabProjectClient
        )
    }

    fun addPlatformUdfModule(
        serviceRuntime: ServiceRuntime,
        udfSourceDirectory: String,
        gitlabProjectClient: GitlabProjectClient,
    ) {
        protoGenerator.addProjectModule(
            sourceDirectory = udfSourceDirectory,
            gradleBuildCommand = udfSourceDirectory.replace('/', ':'),
            gitlabProjectClient = gitlabProjectClient
        )
        protoGenerator.addUdfModuleDependencyToPlatformService(
            buildGradlePath = Path.of(serviceRuntime.sourceDirectory).resolve("build.gradle"),
            udfSourceDirectory = udfSourceDirectory,
            platformProduct = serviceRuntime.namespace,
            gitlabProjectClient = gitlabProjectClient
        )
    }
}
