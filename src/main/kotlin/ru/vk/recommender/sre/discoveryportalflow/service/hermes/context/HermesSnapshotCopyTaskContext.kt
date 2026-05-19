package ru.vk.recommender.sre.discoveryportalflow.service.hermes.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.model.HermesSnapshotDescriptor
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.RuntimeTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

@JsonIgnoreProperties(ignoreUnknown = true)
data class HermesSnapshotCopyTaskContext(
    val runtime: RuntimeTaskContext,
    val baseShardsCount: Int,
    val snapshotTypeIds: List<String>,
    var resolvedProdSnapshots: List<HermesSnapshotDescriptor> = emptyList(),
) : FlowTaskContext {

    val recommenderName: String
        get() = runtime.recommenderName

    val services: List<ServiceRuntime>
        get() = runtime.services
}
