package ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.generator

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabProjectClient
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.writer.ServiceTemplateTreeRenderer
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.buildBaseReplacements
import java.nio.file.Path
import java.util.regex.Pattern

class ProtoGenerator(
    private val templateTreeRenderer: ServiceTemplateTreeRenderer,
) {

    fun generateProtoApis(
        recommender: RecommenderRuntime,
        gitlabProjectClient: GitlabProjectClient
    ) {
        templateTreeRenderer.renderTemplateDirectories(
            templateDirectories = listOf(PROTO_TEMPLATE_DIRECTORY),
            templateReplacements = buildBaseReplacements(recommender.recommenderName) + mapOf(
                "\${RecommenderProtoRoot}" to recommender.recommenderRoot
                    .removePrefix("recommender/")
                    .trim('/'),
            ),
            gitlabProjectClient = gitlabProjectClient
        )
    }

    fun updateRecommenderType(recommender: RecommenderRuntime, gitlabProjectClient: GitlabProjectClient) {
        gitlabProjectClient.updateFile(
            filePath = "domain/src/main/java/ru/vk/recommender/publicapi/RecommenderType.java"
        ) { content ->
            val pattern = Pattern.compile("(?:[A-Z\\d]+_){0,}[A-Z\\d]+\\((\\d+)(?:,|\\)).*")
            val lines = content.split("\n").toMutableList()

            var lastMatchIndex: Int? = null
            val usedNumbers = mutableSetOf<Int>()
            val enumName = recommender.names.enumName

            lines.forEachIndexed { index, line ->
                val matcher = pattern.matcher(line)
                if (matcher.find()) {
                    if (line.contains(enumName)) {
                        return@updateFile content
                    }
                    usedNumbers += matcher.group(1).toInt()
                    lastMatchIndex = index
                }
            }

            val insertionIndex = requireNotNull(lastMatchIndex) { "Cannot find insertion point in path" }
            val nextNumber = (usedNumbers.maxOrNull() ?: 0) + 1
            lines.add(insertionIndex + 1, "    $enumName($nextNumber),")
            lines.joinToString(separator = "\n", postfix = "\n")
        }
    }

    fun insertMetaRecommenderConfig(
        recommender: RecommenderRuntime,
        gitlabProjectClient: GitlabProjectClient
    ) {
        gitlabProjectClient.updateFile(
            filePath = "config/src/main/java/ru/vk/recommender/publicapi/CommonMetaRecommenderConfig.java"
        ) { content ->
            val configKey = recommender.names.folderName
            if (content.contains(configKey)) {
                return@updateFile content
            }

            val insertion = buildString {
                appendLine("    @ConfigProperty(key = \"$configKey\")")
                appendLine("    VkMetaRecommenderConfig get${recommender.names.className}MetaRecommenderConfig();")
            }

            val parts = content.split("}")
            require(parts.size == 2) { "Unexpected format in path" }
            parts[0] + insertion + "}" + parts[1]
        }
    }

    fun addProjectModule(
        sourceDirectory: String,
        gradleBuildCommand: String,
        gitlabProjectClient: GitlabProjectClient
    ) {
        gitlabProjectClient.updateFile(
            filePath = "settings_public.gradle"
        ) { content ->
            val anchor = "// <--- recomutils:append_project_module --->\n"
            val module = gradleBuildCommand.removeSuffix(":export")
            val entry = "include '$module'; project(':$module').projectDir = file('$sourceDirectory')\n"

            appendByAnchor(content, anchor, entry) { c ->
                c.contains(sourceDirectory) || c.contains(entry)
            }
        }
    }

    fun addUdfModuleDependencyToPlatformService(
        buildGradlePath: Path,
        udfSourceDirectory: String,
        platformProduct: String,
        gitlabProjectClient: GitlabProjectClient
    ) {
        gitlabProjectClient.updateFile(
            filePath = buildGradlePath
        ) { content ->
            val anchor = "// <--- recomutils:append_udf_module --->\n"
            val udfProject = udfSourceDirectory.replace('/', ':')
            val dependency = "    platformRuntimeOnly(project(\":$udfProject\"), \"$platformProduct\")\n"

            appendByAnchor(content, anchor, dependency) { c ->
                c.contains(udfProject) || c.contains(dependency)
            }
        }
    }

    private fun appendByAnchor(
        content: String,
        anchor: String,
        entry: String,
        alreadyExists: (String) -> Boolean,
    ): String {
        if (alreadyExists(content)) {
            return content
        }

        require(content.contains(anchor)) { "Anchor '$anchor' not found in path" }
        val parts = content.split(anchor)
        require(parts.size == 2) { "Expected single anchor '$anchor' in path" }
        return parts[0] + entry + anchor + parts[1]
    }


    private companion object {
        private const val PROTO_TEMPLATE_DIRECTORY = "shared/proto"
    }
}
