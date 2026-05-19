package ru.vk.recommender.sre.discoveryportalflow.service.toggles.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "toggles")
class TogglesProperties {
    var webhookToken: String? = null
    var onlineGoldenSourceTenantProjectId: Int? = null
    var offlineGoldenSourceTenantProjectId: Int? = null
    var togglesBaseUrl: String? = null
    var zeusRolesJsonPath: String? = null
    var jiraTaskRoleCreation: String? = null
}
