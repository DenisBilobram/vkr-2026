package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.renderer

import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostBackendTestsPayload

object ServicehostBackendsYamlRenderer {

    fun render(backendTestsPayload: ServicehostBackendTestsPayload): String {
        return buildString {
            appendLine("components:")
            backendTestsPayload.components.toSortedMap().forEach { (componentName, componentSettings) ->
                appendLine("  $componentName:")
                appendLine("    tc_job: \"${escapeYamlString(componentSettings.teamcityJobName)}\"")
            }
        }
    }

    private fun escapeYamlString(rawValue: String): String {
        return rawValue
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }
}
