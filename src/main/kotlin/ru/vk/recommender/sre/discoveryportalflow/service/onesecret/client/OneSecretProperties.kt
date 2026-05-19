package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.client

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "onesecret")
class OneSecretProperties {
    var baseUrl: String = "https://onesecret.nda.example.invalid"
    var token: String = ""
    var requestTimeoutMs: Long = 10000
    var tlsCertificatePath: String? = null
}
