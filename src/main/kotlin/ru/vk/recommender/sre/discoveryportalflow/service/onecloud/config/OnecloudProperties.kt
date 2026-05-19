package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "onecloud")
class OnecloudProperties {
    var datacenterBaseUrlTemplate: String = "https://onecloud-%s.nda.example.invalid/api"
    var namespace: String = "public"
    var connectTimeoutMs: Long = 5000
    var requestTimeoutMs: Long = 30000
    var tlsKeyStorePath: String? = null
    var tlsKeyStorePassword: String? = null
    var tlsKeyStoreType: String = "PKCS12"
    var tlsTrustedCertificatePaths: List<String> = emptyList()
}
