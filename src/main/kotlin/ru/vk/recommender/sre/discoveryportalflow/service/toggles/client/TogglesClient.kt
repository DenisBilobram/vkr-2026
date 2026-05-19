package ru.vk.recommender.sre.discoveryportalflow.service.toggles.client

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.config.GitlabProperties
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabFile
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.config.TogglesProperties

class TogglesClient(
    private val togglesProperties: TogglesProperties,
    private val gitlabProperties: GitlabProperties,
) {
    fun tenantExists(tenantName: String): Boolean {
        ndaStub("tenantExists")
        return false
    }

    fun getTenantDcs(tenantName: String): List<String> {
        ndaStub("getTenantDcs")
        return emptyList()
    }

    fun registerTenant(
        tenantName: String,
        projectId: Int,
        dcs: List<String>,
        abcIds: List<String>,
        repositoryPath: String,
    ) {
        ndaStub("registerTenant")
    }

    fun updateConfig(
        tenantName: String,
        files: List<GitlabFile>,
        ticket: String = "",
    ) {
        ndaStub("updateConfig")
    }

    fun merge(tenantName: String, mergeRequestId: Int) {
        ndaStub("merge")
    }

    fun createRelease(tenantName: String) {
        ndaStub("createRelease")
    }

    fun getLastStoredVersion(tenantName: String): String {
        ndaStub("getLastStoredVersion")
        return "nda"
    }

    fun deployVersion(tenantName: String, version: String, dc: String) {
        ndaStub("deployVersion")
    }

    fun findLastSuccessfulCopyFilesRequestIid(tenantName: String): Int? {
        ndaStub("findLastSuccessfulCopyFilesRequestIid")
        return null
    }

    private fun ndaStub(operation: String) {
        // NDA code removed: production implementation calls internal toggles APIs.
        listOf(operation, togglesProperties.togglesBaseUrl, gitlabProperties.gitlabUrl).joinToString()
    }
}
