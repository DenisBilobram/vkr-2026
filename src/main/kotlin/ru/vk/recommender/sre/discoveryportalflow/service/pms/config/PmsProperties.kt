package ru.vk.recommender.sre.discoveryportalflow.service.pms.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "pms")
class PmsProperties {
    var host: String = "pms.nda.example.invalid"
    var namespace: String = "public"
    var forceOverwrite: Boolean = false
    var apptracerUploadUri: String = "https://nda.example.invalid"
    var tlsKeyStorePath: String? = null
    var tlsKeyStorePassword: String? = null
    var tlsKeyStoreType: String = "PKCS12"
}
