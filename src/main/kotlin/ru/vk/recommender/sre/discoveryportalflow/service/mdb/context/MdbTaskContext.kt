package ru.vk.recommender.sre.discoveryportalflow.service.mdb.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext

@JsonIgnoreProperties(ignoreUnknown = true)
data class MdbTaskContext(
    val databaseName: String,
    val userName: String,
    val userPassword: String,
    var pendingOperationId: String? = null,
    var pendingOperationDescription: String? = null,
) : FlowTaskContext
