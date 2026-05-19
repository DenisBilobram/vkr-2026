package ru.vk.recommender.sre.discoveryportalflow.service.recom

enum class ServiceType(
    val shortName: String,
    val displayName: String,
    val usedInDb: Boolean = false
) {
    BASE("base", "Base Recommender", true),
    META("meta", "Meta Recommender", true),
    GATEWAY("gateway", "Gateway", true),
    PLATFORM_GATEWAY("platform_gateway", "Platform Gateway"),
    GRPC_PROXY("grpc_proxy", "GRPC Proxy", true),
    MEDIATOR("mediator", "Mediator"),
    YT_PROXY("yt_proxy", "YT Proxy", true),
    FACTOR_PROXY("factor_proxy", "Factor Proxy"),
    SELECTORS("selectors", "Selectors"),
    WORKER("worker", "Worker"),
    SNAPSHOTS_BUILDER("snapshots_builder", "Snapshots Builder"),
    META_I2I("meta_i2i", "Meta I2I"),
    BASE_I2I("base_i2i", "Base I2I"),
    SCHEDULER_I2I("scheduler_i2i", "Scheduler I2I"),;

    companion object {
        fun fromShortName(shortName: String): ServiceType =
            ServiceType.entries.firstOrNull { it.shortName.equals(shortName, ignoreCase = true) }
                ?: throw UnknownServiceTypeException(shortName)
    }
}

class UnknownServiceTypeException(type: String) : RuntimeException("Unknown service type: $type")
