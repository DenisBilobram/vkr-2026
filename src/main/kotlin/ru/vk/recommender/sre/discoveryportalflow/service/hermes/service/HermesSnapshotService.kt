package ru.vk.recommender.sre.discoveryportalflow.service.hermes.service

import ru.vk.recommender.sre.discoveryportalflow.service.hermes.client.SnapshotsBuilderClient
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.config.HermesProperties
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.context.HermesSnapshotCopyTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.model.HermesSnapshotBuildResult
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.model.HermesSnapshotCopyResult
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.model.HermesSnapshotDescriptor
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.model.HermesSnapshotResolution
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import java.net.http.HttpClient

class HermesSnapshotService(
    private val hermesProperties: HermesProperties,
    private val snapshotsBuilderClient: SnapshotsBuilderClient,
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
) {

    fun triggerProdSnapshotBuilds(context: HermesSnapshotCopyTaskContext): HermesSnapshotBuildResult {
        // NDA code removed: production implementation builds and sends internal Hermes snapshot requests.
        return HermesSnapshotBuildResult(
            builderBaseUrl = hermesProperties.prodServerApiAddress ?: "NDA code removed",
            triggeredRequestsCount = 0,
        )
    }

    fun addMapping(serviceRuntime: ServiceRuntime): Boolean {
        // NDA code removed: production implementation adds mappings through an internal Hermes API.
        return false
    }

    fun resolveProdSnapshots(typeIds: List<String>): HermesSnapshotResolution {
        // NDA code removed: production implementation resolves latest production snapshot metadata.
        return HermesSnapshotResolution(
            resolvedSnapshots = emptyList(),
            missingTypeIds = typeIds,
        )
    }

    fun copySnapshotsMetaToTesting(snapshots: List<HermesSnapshotDescriptor>): HermesSnapshotCopyResult {
        // NDA code removed: production implementation copies snapshot metadata between internal Hermes clusters.
        return HermesSnapshotCopyResult(copiedCount = 0, skippedCount = snapshots.size)
    }
}
