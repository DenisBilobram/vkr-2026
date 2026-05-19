package ru.vk.recommender.sre.discoveryportalflow.service.ocs.service

import ru.vk.recommender.sre.discoveryportalflow.service.ocs.client.OcsClient
import ru.vk.recommender.sre.discoveryportalflow.service.ocs.context.OcsTaskContext

class DictionaryProjectService(
    private val ocsClient: OcsClient,
) {

    fun initializeDictionaryProject(taskContext: OcsTaskContext) {
        val requestPayload = mapOf(
            "project_name" to taskContext.recommenderName,
            "owner" to taskContext.serviceOwner,
            "copy_from" to DICTIONARY_GOLDEN_SOURCE_PROJECT_NAME,
        )

        ocsClient.postJson("/common/dictionary/project", requestPayload)
        println("NDA code removed: verify dictionary project '${taskContext.recommenderName}' in the internal dictionary system.")
        println("NDA code removed: verify generated access changes in the internal Git repository.")
    }

    private companion object {
        private const val DICTIONARY_GOLDEN_SOURCE_PROJECT_NAME = "golden-source"
    }
}
