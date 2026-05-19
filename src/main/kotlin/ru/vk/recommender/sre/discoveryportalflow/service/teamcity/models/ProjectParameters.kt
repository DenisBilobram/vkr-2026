package ru.vk.recommender.sre.discoveryportalflow.service.teamcity.models

data class ProjectParameters(
    var branchFilter: String = "",
    val deployServices: String = "",
    val deployCloudSubqueue: String = "",
    val apphostUrl: String = "",
    val deployApphostTests: String = "",
    val drillsProjects: String = "",
    var servicesToDeploy: String = "",
    val teamsChatId: String = "",
    val shootingСomponent: String = "",
    val shootingGraph: String = "",
) {
    fun toMap(): Map<String, String> = mapOf(
        "branch_filter" to branchFilter,
        "deploy_services" to deployServices,
        "deploy_cloud_subqueue" to deployCloudSubqueue,
        "apphost_url" to apphostUrl,
        "deploy_apphost_tests" to deployApphostTests,
        "drills_projects" to drillsProjects,
        "services_to_deploy" to servicesToDeploy,
        "teams_chat_id" to teamsChatId,
        "shooting_component" to shootingСomponent,
        "shooting_graph" to shootingGraph,
    ).filterValues { it.isNotEmpty() }

    fun withBranchFilter(branchFilter: String): ProjectParameters {
        this.branchFilter = branchFilter
        return this
    }

    fun withServicesToDeploy(servicesToDeploy: String): ProjectParameters {
        this.servicesToDeploy = servicesToDeploy
        return this
    }
}
