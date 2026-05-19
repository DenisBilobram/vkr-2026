package ru.vk.recommender.sre.discoveryportalflow.service.yt.model

enum class YtCluster {
    JUPITER,
    SATURN,
    ;

    val clusterName: String
        get() = name.lowercase()
}
