package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec.SECRET_PLACEHOLDER

@JsonIgnoreProperties(ignoreUnknown = true)
data class AppTracerInfo(
    val token: String = SECRET_PLACEHOLDER,
)
