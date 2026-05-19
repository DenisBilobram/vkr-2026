package ru.vk.recommender.sre.discoveryportalflow.service.teamcity.models

enum class Environment(val value: String) {
    TESTING("testing"),
    CANARY("canary"),
    PRODUCTION("production");
}
