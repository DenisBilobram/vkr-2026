package ru.vk.recommender.sre.discoveryportalflow.service.vkteams.client.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Duration

@ConfigurationProperties(prefix = "vkteams.client")
@Component
class VkTeamsClientProperties {
    var baseUrl: String = "https://messenger.nda.example.invalid/bot/v1"
    var token: String = ""
    var timeout: Duration = Duration.ofSeconds(30)
    var connectTimeout: Duration = Duration.ofSeconds(10)
}
