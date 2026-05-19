package ru.vk.recommender.sre.discoveryportalflow.service.yt.client

import ru.vk.recommender.sre.discoveryportalflow.service.yt.model.YtCluster

class YtRuntimeClients(
    val user: String,
    val tmpDir: String,
    val ytCluster: YtCluster,
) : AutoCloseable {

    override fun close() {
        // NDA code removed: production implementation closes YT RPC clients here.
    }
}
