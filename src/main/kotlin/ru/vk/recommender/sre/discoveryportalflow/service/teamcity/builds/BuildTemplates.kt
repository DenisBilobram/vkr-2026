package ru.vk.recommender.sre.discoveryportalflow.service.teamcity.builds

import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.models.Environment

abstract class BuildConfig {
    abstract val name: String
    abstract val id: String
    abstract val projectId: String
    abstract val templateId: String?
    abstract val properties: List<Property>
    abstract val steps: Map<String, Any>?

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "name" to name,
            "id" to id,
            "project" to mapOf("id" to projectId)
        )
        templateId?.let {
            map["templates"] = mapOf("buildType" to listOf(mapOf("id" to it)))
        }
        steps?.let {
            map["steps"] = it
        }
        if (properties.isNotEmpty()) {
            map["parameters"] = mapOf(
                "property" to properties.map { mapOf("name" to it.name, "value" to it.value) },
                "count" to properties.size
            )
        }
        return map
    }
}

data class Property(val name: String, val value: String)

class DockerImageBuild(
    override val projectId: String,
    val component: String,
    override val name: String = "Build docker image one-cloud",
    override val id: String = "${projectId}_BuildDockerImageOneCloud",
    override val templateId: String? = "Public_Recommender_BuildDockerImageOneCloud",
    override val properties: List<Property> = listOf(
        Property("component", component),
        Property("base_image_name", "registry.nda.example.invalid/recommender-java21-base:stable"),
        Property("scripts_path", ""),
        Property("latest", "-l")
    ),
    override val steps: Map<String, Any>? = null
) : BuildConfig()

class DeployRecommenderBuild(
    override val projectId: String,
    val component: String,
    val dc: String,
    val env: Environment,
    override val name: String = "Deploy ${env.value} ${dc.uppercase()}",
    override val id: String = "${projectId}_Deploy${env.value.replaceFirstChar { it.uppercase() }}${dc}Cloud",
    override val templateId: String? = "Public_Recommender_DeployToCloud",
    override val properties: List<Property> = run {
        val props = mutableListOf(
            Property("dc", dc.uppercase()),
            Property("deploy_dc", dc.lowercase()),
            Property("component", component),
            Property("deploy_environment", env.value),
        )
        props
    },
    override val steps: Map<String, Any>? = null
) : BuildConfig()

class TestApphostGraphsBuild(
    override val projectId: String,
    override val name: String = "Test apphost graphs",
    override val id: String = "${projectId}_TestApphostGraphs",
    override val templateId: String? = null,
    override val properties: List<Property> = emptyList(),
    override val steps: Map<String, Any>? = mapOf(
        "step" to listOf(
            mapOf(
                "name" to "Export",
                "type" to "gradle-runner",
                "properties" to mapOf(
                    "property" to listOf(
                        mapOf("name" to "ui.gradleRunner.gradle.tasks.names", "value" to ":tools:apphost-query:export"),
                        mapOf("name" to "use.gradle.wrapper", "value" to "true")
                    )
                )
            ),
            mapOf(
                "name" to "Run shooting",
                "type" to "gradle-runner",
                "properties" to mapOf(
                    "property" to listOf(
                        mapOf(
                            "name" to "ui.gradleRunner.gradle.tasks.names",
                            "value" to ":tools:apphost-query:test %deploy_apphost_tests% -i -Dapphost_url=%apphost_url%"
                        ),
                        mapOf("name" to "use.gradle.wrapper", "value" to "true")
                    )
                )
            )
        )
    )
) : BuildConfig()

class DeployToProductionButton(
    override val projectId: String,
    override val name: String = "Deploy to production",
    override val id: String = "${projectId}_DeployToProductionButton",
    override val templateId: String? = null,
    override val properties: List<Property> = emptyList(),
    override val steps: Map<String, Any>? = null,
) : BuildConfig()

class TestApphostGraphsByTogglesBuild(
    override val projectId: String,
    val togglesVersion: String = "",
    override val name: String = "Test apphost graphs by toggles",
    override val id: String = "${projectId}_TestApphostGraphsByToggles",
    override val templateId: String? = null,
    override val properties: List<Property> = listOf(Property("toggles_version", togglesVersion)),
    override val steps: Map<String, Any>? = mapOf(
        "step" to listOf(
            mapOf(
                "name" to "Export",
                "type" to "gradle-runner",
                "properties" to mapOf(
                    "property" to listOf(
                        mapOf("name" to "ui.gradleRunner.gradle.tasks.names", "value" to ":tools:apphost-query:export"),
                        mapOf("name" to "use.gradle.wrapper", "value" to "true")
                    )
                )
            ),
            mapOf(
                "name" to "Run shooting",
                "type" to "gradle-runner",
                "properties" to mapOf(
                    "property" to listOf(
                        mapOf(
                            "name" to "ui.gradleRunner.gradle.tasks.names",
                            "value" to ":tools:apphost-query:test --tests ApphostShootingTest.testRecommenderVkPosts -i -Dtoggles_version=%toggles_version%"
                        ),
                        mapOf("name" to "use.gradle.wrapper", "value" to "true")
                    )
                )
            )
        )
    )
) : BuildConfig()

class GitlabCITestApphostGraphsBuild(
    override val projectId: String,
    override val name: String = "[Gitlab CI] Test apphost graphs",
    override val id: String = "${projectId}_GitlabCITestApphostGraphs",
    override val templateId: String? = "Public_TestApphostGraphsGitlabCI",
    override val properties: List<Property> = emptyList(),
    override val steps: Map<String, Any>? = null
) : BuildConfig()

class CreateReleaseBuild(
    override val projectId: String,
    val component: String,
    override val name: String = "Create release",
    override val id: String = "${projectId}_CreateRelease",
    override val templateId: String? = "Public_Recommender_CreateRelease",
    override val properties: List<Property> = listOf(
        Property("component", component),
        Property("project", component)
    ),
    override val steps: Map<String, Any>? = null
) : BuildConfig()

class CloseReleaseBuild(
    override val projectId: String,
    val component: String,
    override val name: String = "Close release",
    override val id: String = "${projectId}_CloseRelease",
    override val templateId: String? = "Public_Recommender_CloseRelease",
    override val properties: List<Property> = listOf(
        Property("component", component),
        Property("project", component)
    ),
    override val steps: Map<String, Any>? = null
) : BuildConfig()

class CherryPickBuild(
    override val projectId: String,
    override val name: String = "Cherry-pick",
    override val id: String = "${projectId}_CherryPick",
    override val templateId: String? = "Public_Recommender_CherryPick",
    override val properties: List<Property> = emptyList(),
    override val steps: Map<String, Any>? = null
) : BuildConfig()
