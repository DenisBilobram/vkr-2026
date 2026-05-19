package ru.vk.recommender.sre.discoveryportalflow.service.yt.context

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class YtRuntimeCredentials(
    @JsonAlias("yt.users")
    val users: String,
    @JsonAlias("yt.tokens")
    val tokens: String,
)
