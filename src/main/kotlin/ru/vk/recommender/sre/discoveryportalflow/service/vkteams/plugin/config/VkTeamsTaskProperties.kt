package ru.vk.recommender.sre.discoveryportalflow.service.vkteams.plugin.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "vkteams.task")
class VkTeamsTaskProperties {
    var maxUsersAddAllowed: Int = 7
    var areChatsPublic: Boolean = false
    var isChatCreateAllowed: Boolean = true
    var mandatoryUsersToAdd: List<String> = emptyList()
}
