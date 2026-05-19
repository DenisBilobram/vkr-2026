package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabFile
import java.nio.file.Path

class ServicehostShootingTestsWriter(
    private val gitlabClient: GitlabClient
) {

    fun appendGraphTests(
        workspaceRoot: Path,
        graphNames: List<String>,
    ): GitlabFile {
        val shootingTestsPath = workspaceRoot.resolve(DEFAULT_SHOOTING_TESTS_RELATIVE_PATH)

        return gitlabClient.updateFile(gitlabClient.getRepositoryProjectId(), shootingTestsPath) { fileContent ->
            require(fileContent.contains(SHOOTING_TESTS_ANCHOR)) {
                "Failed to find anchor in apphost shooting tests file: $shootingTestsPath"
            }

            val methodsToAppend = buildString {
                graphNames.forEach { graphName ->
                    if (fileContent.contains(graphName)) {
                        return@forEach
                    }

                    val methodSuffix = graphName.split("-")
                        .joinToString(separator = "") { chunk -> chunk.replaceFirstChar(Char::uppercase) }

                    appendLine("    // This entry was added by recomutils. Do not edit manually")
                    appendLine("    @Test")
                    appendLine("    @DisplayName(\"Test $graphName\")")
                    appendLine("    public void test$methodSuffix() {")
                    appendLine("        runE2EShootings(\"$graphName\");")
                    appendLine("    }")
                    appendLine()
                }
            }

            if (methodsToAppend.isBlank()) {
                return@updateFile fileContent
            }

            val anchorParts = fileContent.split(SHOOTING_TESTS_ANCHOR)
            require(anchorParts.size == 2) {
                "More than one anchor in apphost shooting tests file: $shootingTestsPath"
            }

            anchorParts[0] + methodsToAppend + SHOOTING_TESTS_ANCHOR + anchorParts[1]
        }
    }

    private companion object {
        private const val SHOOTING_TESTS_ANCHOR = "    // <--- recomutils:append_apphost_shooting_tests --->\n"
        private val DEFAULT_SHOOTING_TESTS_RELATIVE_PATH = Path.of(
            "tools",
            "apphost-query",
            "src",
            "test",
            "java",
            "ApphostShootingTest.java",
        )
    }
}
