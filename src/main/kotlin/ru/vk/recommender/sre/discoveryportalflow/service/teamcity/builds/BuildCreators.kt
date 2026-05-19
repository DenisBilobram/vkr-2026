package ru.vk.recommender.sre.discoveryportalflow.service.teamcity.builds

import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.models.Environment

fun createBuildDockerImageOnecloudBuild(projectId: String, component: String): Map<String, Any> =
    DockerImageBuild(projectId, component).toMap()

fun createDeployRecommenderCloudBuild(
    projectId: String,
    component: String,
    dc: String,
    env: Environment,
): Map<String, Any> =
    DeployRecommenderBuild(projectId, component, dc, env).toMap()

fun createTestApphostGraphs(projectId: String): Map<String, Any> =
    TestApphostGraphsBuild(
        projectId,
        name = "Test apphost graphs",
        id = "${projectId}_TestApphostGraphs"
    ).toMap()

fun createGitlabCITestApphostGraphs(projectId: String): Map<String, Any> =
    GitlabCITestApphostGraphsBuild(projectId).toMap()

fun createDeployToProductionButton(projectId: String): Map<String, Any> =
    DeployToProductionButton(projectId).toMap()

fun createTestApphostGraphsByToggles(
    projectId: String,
    togglesVersion: String = ""
): Map<String, Any> =
    TestApphostGraphsByTogglesBuild(
        projectId,
        togglesVersion,
        name = "Test apphost graphs by toggles",
        id = "${projectId}_TestApphostGraphsByToggles"
    ).toMap()

fun createCreateReleaseBuild(projectId: String, component: String): Map<String, Any> =
    CreateReleaseBuild(projectId, component).toMap()

fun createCloseReleaseBuild(projectId: String, component: String): Map<String, Any> =
    CloseReleaseBuild(projectId, component).toMap()

fun createCherryPickBuild(projectId: String): Map<String, Any> =
    CherryPickBuild(projectId).toMap()
