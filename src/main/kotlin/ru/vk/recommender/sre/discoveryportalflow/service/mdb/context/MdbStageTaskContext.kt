package ru.vk.recommender.sre.discoveryportalflow.service.mdb.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context.OneSecretWriteTaskContext

@JsonIgnoreProperties(ignoreUnknown = true)
data class MdbStageTaskContext(
    val mdbTaskContext: MdbTaskContext,
    val oneSecretWriteTaskContext: OneSecretWriteTaskContext,
) : FlowTaskContext
