package ru.vk.recommender.sre.discoveryportalflow.service.hermes.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "hermes")
class HermesProperties {
    var prodServerApiAddress: String? = null
    var testingServerApiAddress: String? = null
}
