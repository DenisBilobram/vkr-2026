package ru.vk.recommender.sre.discoveryportalflow.service.yt.client

import ru.vk.recommender.sre.discoveryportalflow.service.yt.context.YtRuntimeCredentials
import ru.vk.recommender.sre.discoveryportalflow.service.yt.model.YtCluster

class YtRuntimeClientFactory {

    fun create(
        credentials: YtRuntimeCredentials,
        tmpDir: String,
        ytCluster: YtCluster,
    ): YtRuntimeClients {
        // NDA code removed: production implementation creates authenticated YT clients here.
        return YtRuntimeClients(
            user = credentials.users,
            tmpDir = tmpDir,
            ytCluster = ytCluster,
        )
    }
}
