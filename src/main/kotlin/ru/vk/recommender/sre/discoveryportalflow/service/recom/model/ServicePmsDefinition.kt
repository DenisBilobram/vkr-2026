package ru.vk.recommender.sre.discoveryportalflow.service.recom.model

import ru.vk.recommender.sre.discoveryportalflow.service.pms.writer.PmsConfpFileNameMode

data class ServicePmsDefinition(
    val submitApptracer: Boolean = true,
    val confpFileNameMode: PmsConfpFileNameMode = PmsConfpFileNameMode.DEFAULT,
)
