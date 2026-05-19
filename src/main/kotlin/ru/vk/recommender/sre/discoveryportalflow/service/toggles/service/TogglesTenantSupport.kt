package ru.vk.recommender.sre.discoveryportalflow.service.toggles.service

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.config.GitlabProperties

class TogglesTenantSupport(
    private val gitlabProperties: GitlabProperties,
) {

    fun buildGitlabProjectName(tenantName: String): String {
        return tenantName.split("-")
            .joinToString(" ") { segment -> segment.replaceFirstChar(Char::uppercaseChar) } + " Toggles"
    }

    fun buildGitlabProjectPath(tenantName: String): String {
        return "$tenantName-toggles"
    }

    fun buildGitlabRepositoryPath(projectPath: String): String {
        return "${gitlabProperties.aiNamespacePath}/$projectPath"
    }

    fun buildWebhookUrl(tenantName: String): String {
        return "https://toggles.nda.example.invalid/v2/api/$tenantName/integration/web-hook"
    }

    fun buildDeployTargets(dcs: List<String>): List<String> {
        return buildList {
            add("canary")
            addAll(dcs.map(String::lowercase))
        }.distinct()
    }
}
