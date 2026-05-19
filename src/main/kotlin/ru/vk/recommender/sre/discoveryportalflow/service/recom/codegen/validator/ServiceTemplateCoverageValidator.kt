package ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.validator

import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomServiceRegistry
import ru.vk.recommender.sre.discoveryportalflow.service.recom.resolver.WorkspaceResolver
import java.nio.file.Files
import java.nio.file.Path

class ServiceTemplateCoverageValidator(
    private val recomServiceRegistry: RecomServiceRegistry,
) {

    fun validateTemplateCoverage() {
        val templateRootDirectory = WorkspaceResolver.resolveOrchestratorPath(
            orchestratorRelativePath = TEMPLATE_ROOT_RELATIVE_PATH,
        )
        require(Files.isDirectory(templateRootDirectory)) {
            "Template root directory does not exist: $templateRootDirectory"
        }

        val expectedServiceTemplateDirectories = recomServiceRegistry.allServices()
            .mapNotNull { service -> service.templateDirectory }
            .toSet()
        val discoveredServiceTemplateDirectories = discoverServiceTemplateDirectories(templateRootDirectory)

        val missingTemplateDirectories = (expectedServiceTemplateDirectories - discoveredServiceTemplateDirectories).sorted()
        require(missingTemplateDirectories.isEmpty()) {
            "Missing template directories for service types: ${missingTemplateDirectories.joinToString(", ")}"
        }

        val unexpectedTemplateDirectories =
            (discoveredServiceTemplateDirectories - expectedServiceTemplateDirectories).sorted()
        require(unexpectedTemplateDirectories.isEmpty()) {
            "Unexpected template directories without mapped service types: ${unexpectedTemplateDirectories.joinToString(", ")}"
        }

        val missingSharedTemplateDirectories = recomServiceRegistry.allServices()
            .flatMap { service -> service.sharedTemplateDirectories }
            .distinct()
            .filter { sharedTemplateDirectory ->
                !Files.isDirectory(templateRootDirectory.resolve(sharedTemplateDirectory))
            }
        require(missingSharedTemplateDirectories.isEmpty()) {
            "Missing shared template directories: ${missingSharedTemplateDirectories.joinToString(", ")}"
        }
    }

    private fun discoverServiceTemplateDirectories(templateRootDirectory: Path): Set<String> {
        return Files.list(templateRootDirectory).use { pathStream ->
            pathStream
                .filter { directoryPath -> Files.isDirectory(directoryPath) }
                .map { directoryPath -> directoryPath.fileName.toString() }
                .filter { directoryName ->
                    directoryName !in IGNORED_TEMPLATE_DIRECTORIES && !directoryName.startsWith(".")
                }
                .toList()
                .toSet()
        }
    }

    private companion object {
        private const val TEMPLATE_ROOT_RELATIVE_PATH = "src/main/resources/templates/genericrecom/codegen/service-tree"
        private val IGNORED_TEMPLATE_DIRECTORIES = setOf("__pycache__", "shared")
    }
}
