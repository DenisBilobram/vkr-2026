package ru.vk.recommender.sre.discoveryportalflow.service.teamcity.client

import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.config.TeamcityProperties
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.models.TeamCityBuild

class TeamCityClient(
    private val properties: TeamcityProperties,
) {

    fun checkIsProjectAlreadyExist(projectId: String): Boolean {
        ndaStub("checkIsProjectAlreadyExist")
        return false
    }

    fun checkIsBuildAlreadyExist(buildId: String): Boolean {
        ndaStub("checkIsBuildAlreadyExist")
        return false
    }

    fun createSubProject(projectId: String, projectName: String, parentProjectId: String): String {
        ndaStub("createSubProject")
        return projectId
    }

    fun addProjectParameters(projectId: String, newParameters: Map<String, String>) {
        ndaStub("addProjectParameters")
    }

    fun createBuild(build: Map<String, Any>): Map<String, Any>? {
        ndaStub("createBuild")
        return build
    }

    fun createBuilds(builds: List<Map<String, Any>>) {
        ndaStub("createBuilds")
    }

    fun triggerBuild(buildTypeId: String, branch: String): String? {
        ndaStub("triggerBuild")
        return null
    }

    fun triggerAddingComponent(components: List<String>): String? {
        ndaStub("triggerAddingComponent")
        return null
    }

    fun triggerTask(taskId: String, props: List<Map<String, Any>>): String? {
        ndaStub("triggerTask")
        return null
    }

    fun getBuildById(buildId: String): TeamCityBuild? {
        ndaStub("getBuildById")
        return null
    }

    private fun ndaStub(operation: String) {
        // NDA code removed: production implementation calls internal TeamCity APIs.
        properties.token.length + operation.length
    }
}
