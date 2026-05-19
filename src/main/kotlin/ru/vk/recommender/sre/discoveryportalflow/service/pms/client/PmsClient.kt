package ru.vk.recommender.sre.discoveryportalflow.service.pms.client

import com.fasterxml.jackson.databind.ObjectMapper
import ru.vk.recommender.sre.discoveryportalflow.service.pms.config.PmsProperties

class PmsClient(
    private val objectMapper: ObjectMapper,
    private val pmsProperties: PmsProperties,
) {

    fun updateProperties(
        applicationName: String,
        onecloudPmsHosts: List<String>,
        properties: Map<String, String>,
        forceOverwrite: Boolean = pmsProperties.forceOverwrite,
    ) {
        // NDA code removed: production implementation reads and updates internal PMS properties.
        objectMapper.createObjectNode()
            .put("applicationName", applicationName)
            .put("hosts", onecloudPmsHosts.joinToString(","))
            .put("propertiesCount", properties.size)
            .put("forceOverwrite", forceOverwrite)
    }
}
