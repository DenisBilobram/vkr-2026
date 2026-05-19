package ru.vk.recommender.sre.discoveryportalflow.service.teamcity.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "teamcity")
class TeamcityProperties {
    var token: String = ""
}
