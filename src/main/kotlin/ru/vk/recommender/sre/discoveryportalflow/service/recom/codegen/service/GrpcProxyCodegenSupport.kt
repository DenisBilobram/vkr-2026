package ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.service

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabProjectClient
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import java.nio.file.Path

class GrpcProxyCodegenSupport {

    fun updateCommonFiles(
        recommenderRuntime: RecommenderRuntime,
        serviceRuntime: ServiceRuntime,
        serviceSourceDirectory: Path,
        gitlabProjectClient: GitlabProjectClient,
    ) {
        val commonGrpcProxyDirectory = Path.of(COMMON_GRPC_PROXY_SOURCE_DIRECTORY)
        val projectNames = serviceRuntime.names
        val recommenderNames = recommenderRuntime.names
        val backendEnumName = "SERVICEHOST_${recommenderNames.enumName}"
        val backendValue = "public-servicehost-${recommenderRuntime.recommenderName}"

        appendProfileConstant(
            filePath = serviceSourceDirectory.resolve("src/main/java/ru/vk/recommender/grpcproxy/GrpcProxyProfile.java"),
            profileEnumName = projectNames.enumName,
            profileValue = projectNames.packageName,
            gitlabProjectClient = gitlabProjectClient
        )


        appendBackendEnumConstant(
            filePath = commonGrpcProxyDirectory.resolve(
                "src/main/java/ru/vk/recommender/grpcproxy/common/backend/client/RecommenderBackendName.java",
            ),
            backendEnumName = backendEnumName,
            backendValue = backendValue,
            gitlabProjectClient = gitlabProjectClient
        )

        appendHttpForwarderBackend(
            filePath = commonGrpcProxyDirectory.resolve(
                "src/main/java/ru/vk/recommender/grpcproxy/common/handler/base/BaseHttpHandlerFactory.java",
            ),
            backendEnumName = backendEnumName,
            gitlabProjectClient = gitlabProjectClient
        )

        appendHttpForwarderBackend(
            filePath = commonGrpcProxyDirectory.resolve(
                "src/main/java/ru/vk/recommender/grpcproxy/common/handler/HttpHandlerFactory.java",
            ),
            backendEnumName = backendEnumName,
            gitlabProjectClient = gitlabProjectClient
        )

        appendConfpBackendWeightsTemplate(
            filePath = serviceSourceDirectory.resolve(
                "src/docker/${serviceRuntime.cloudServiceName}-onecloud/etc/confp/resources.d/${serviceRuntime.cloudServiceName}.yml",
            ),
            cloudServiceName = serviceRuntime.cloudServiceName,
            recommenderName = recommenderRuntime.recommenderName,
            gitlabProjectClient = gitlabProjectClient
        )
    }

    private fun appendProfileConstant(
        filePath: Path,
        profileEnumName: String,
        profileValue: String,
        gitlabProjectClient: GitlabProjectClient,
    ) {
        gitlabProjectClient.updateFile(filePath) { content ->
            val constantLine = "    public static final String $profileEnumName = \"$profileValue\";"
            if (content.contains(constantLine)) {
                return@updateFile content
            }

            val closingBraceIndex = content.lastIndexOf('}')
            require(closingBraceIndex >= 0) { "Failed to find class closing brace in $filePath" }

            buildString {
                append(content.substring(0, closingBraceIndex).trimEnd())
                appendLine()
                appendLine(constantLine)
                append(content.substring(closingBraceIndex))
            }
        }
    }

    private fun appendBackendEnumConstant(
        filePath: Path,
        backendEnumName: String,
        backendValue: String,
        gitlabProjectClient: GitlabProjectClient,
    ) {
        gitlabProjectClient.updateFile(filePath) { content ->
            val enumEntry = "    $backendEnumName(\"$backendValue\"),"
            if (content.contains(enumEntry)) {
                return@updateFile content
            }

            val semicolonPattern = Regex("""(?m)^(\s*;)""")
            val semicolonMatch = requireNotNull(semicolonPattern.find(content)) {
                "Failed to find enum terminator in $filePath"
            }
            content.replaceRange(
                semicolonMatch.range.first,
                semicolonMatch.range.last + 1,
                "$enumEntry\n${semicolonMatch.value}",
            )
        }
    }

    private fun appendHttpForwarderBackend(
        filePath: Path,
        backendEnumName: String,
        gitlabProjectClient: GitlabProjectClient,
    ) {
        gitlabProjectClient.updateFile(filePath) { content ->
            val lines = content.split("\n").toMutableList()
            val targetLineIndex = lines.indexOfFirst { line ->
                line.contains("case ") && line.contains("httpForwarderMetrics")
            }
            require(targetLineIndex >= 0) { "Failed to find httpForwarderMetrics switch case in $filePath" }

            val targetLine = lines[targetLineIndex]
            if (targetLine.contains(backendEnumName)) {
                return@updateFile content
            }

            lines[targetLineIndex] = targetLine.replace(
                " -> httpForwarderMetrics;",
                ", $backendEnumName -> httpForwarderMetrics;",
            )
            lines.joinToString("\n") + "\n"
        }
    }

    private fun appendConfpBackendWeightsTemplate(
        filePath: Path,
        cloudServiceName: String,
        recommenderName: String,
        gitlabProjectClient: GitlabProjectClient,
    ) {
        gitlabProjectClient.updateFile(filePath) { content ->
            val propertyName = "grpc-proxy.backend-weights-servicehost-$recommenderName"
            val templateBlock = listOf(
                "  - content: \"{{ pms('$propertyName') }}\"",
                "    dest: /etc/$cloudServiceName/grpc-http-adapter-backend-weights-servicehost-$recommenderName",
                "    cron: \"* * * * *\"",
            ).joinToString(separator = "\n")
            if (content.contains("{{ pms('$propertyName') }}")) {
                return@updateFile content
            }

            val anchor = "  - content: \"{{ pms('grpc-proxy.configuration') }}\""
            val anchorIndex = content.indexOf(anchor)
            require(anchorIndex >= 0) { "Failed to find grpc-proxy.configuration template anchor in $filePath" }

            val insertion = if (content.substring(0, anchorIndex).endsWith("\n")) {
                "$templateBlock\n"
            } else {
                "\n$templateBlock\n"
            }
            return@updateFile content.substring(0, anchorIndex) + insertion + content.substring(anchorIndex)
        }
    }

    private companion object {
        private const val COMMON_GRPC_PROXY_SOURCE_DIRECTORY = "recommender/common/grpc-proxy"
    }
}
