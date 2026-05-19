package ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.writer

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabProjectClient
import ru.vk.recommender.sre.discoveryportalflow.service.recom.resolver.WorkspaceResolver
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.renderTemplate
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class ServiceTemplateTreeRenderer {

    fun renderTemplateDirectories(
        templateDirectories: List<String>,
        templateReplacements: Map<String, String>,
        gitlabProjectClient: GitlabProjectClient,
    ) {
        val serviceTreeRootDirectory = WorkspaceResolver.resolveOrchestratorPath(
            orchestratorRelativePath = TEMPLATE_ROOT_RELATIVE_PATH,
        )

        templateDirectories.forEach { templateDirectory ->
            val templateDirectoryPath = serviceTreeRootDirectory.resolve(templateDirectory)
            require(Files.isDirectory(templateDirectoryPath)) {
                "Template directory does not exist: $templateDirectoryPath"
            }

            Files.walk(templateDirectoryPath).use { paths ->
                paths.filter { Files.isRegularFile(it) }
                    .forEach { templatePath ->
                        val relativeTemplatePath = templateDirectoryPath.relativize(templatePath)
                        val renderedRelativePath = renderRelativePath(relativeTemplatePath, templateReplacements)
                        val renderedContent = renderTemplate(
                            Files.readString(templatePath, StandardCharsets.UTF_8),
                            templateReplacements,
                        )

                        val updateFile = gitlabProjectClient.getFileOptional(renderedRelativePath)
                        updateFile.content = renderedContent
                    }
            }
        }
    }

    fun renderTemplateFiles(
        templateDirectory: String,
        relativeTemplatePaths: List<String>,
        templateReplacements: Map<String, String>,
        gitlabProjectClient: GitlabProjectClient,
    ) {
        val serviceTreeRootDirectory = WorkspaceResolver.resolveOrchestratorPath(
            orchestratorRelativePath = TEMPLATE_ROOT_RELATIVE_PATH,
        )
        val templateDirectoryPath = serviceTreeRootDirectory.resolve(templateDirectory)
        require(Files.isDirectory(templateDirectoryPath)) {
            "Template directory does not exist: $templateDirectoryPath"
        }

        relativeTemplatePaths.forEach { relativeTemplatePath ->
            val templatePath = templateDirectoryPath.resolve(relativeTemplatePath)
            require(Files.isRegularFile(templatePath)) {
                "Template file does not exist: $templatePath"
            }

            val renderedRelativePath = renderRelativePath(Path.of(relativeTemplatePath), templateReplacements)
            val renderedContent = renderTemplate(
                Files.readString(templatePath, StandardCharsets.UTF_8),
                templateReplacements,
            )

            val updateFile = gitlabProjectClient.getFileOptional(renderedRelativePath)
            updateFile.content = renderedContent
        }
    }

    private fun renderRelativePath(
        relativeTemplatePath: Path,
        templateReplacements: Map<String, String>,
    ): Path {
        var renderedPath = Path.of("")
        relativeTemplatePath.forEach { pathSegment ->
            renderedPath = renderedPath.resolve(renderTemplate(pathSegment.toString(), templateReplacements))
        }
        return renderedPath
    }

    private companion object {
        private const val TEMPLATE_ROOT_RELATIVE_PATH = "src/main/resources/templates/genericrecom/codegen/service-tree"
    }
}
