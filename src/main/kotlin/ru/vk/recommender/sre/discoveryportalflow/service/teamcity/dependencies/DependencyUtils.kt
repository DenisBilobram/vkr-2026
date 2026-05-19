package ru.vk.recommender.sre.discoveryportalflow.service.teamcity.dependencies

fun setDependency(build: MutableMap<String, Any>, dependencyBuild: Map<String, Any>, isComposite: Boolean = false) {
    val dependencyId = dependencyBuild["id"]
    require(dependencyId is String) { "dependencyBuild.id must be String" }
    setDependency(build, dependencyId, isComposite)
}

fun setDependency(build: MutableMap<String, Any>, dependencyId: String, isComposite: Boolean = false) {
    val projectNode = build["project"]
    require(projectNode is Map<*, *>) { "build.project must be an object" }

    if (isComposite) {
        build["type"] = "composite"
    }

    val newDependency = mapOf(
        "id" to dependencyId,
        "type" to "snapshot_dependency",
        "source-buildType" to mapOf(
            "id" to dependencyId,
            "projectId" to projectNode["id"]
        ),
        "properties" to mapOf(
            "property" to listOf(
                mapOf("name" to "run-build-if-dependency-failed", "value" to "MAKE_FAILED_TO_START"),
                mapOf("name" to "run-build-if-dependency-failed-to-start", "value" to "MAKE_FAILED_TO_START"),
                mapOf("name" to "run-build-on-the-same-agent", "value" to "false"),
                mapOf("name" to "sync-revisions", "value" to "true"),
                mapOf("name" to "take-started-build-with-same-revisions", "value" to "true"),
                mapOf("name" to "take-successful-builds-only", "value" to "true")
            ),
            "count" to 6
        )
    )

    val currentDependenciesNode = build["snapshot-dependencies"]
    val dependenciesMap = when (currentDependenciesNode) {
        null -> mutableMapOf<Any?, Any?>()
        is MutableMap<*, *> -> currentDependenciesNode.toMutableMap()
        else -> error("build.snapshot-dependencies must be an object")
    }

    val dependencyListNode = dependenciesMap["snapshot-dependency"]
    val dependencyList = when (dependencyListNode) {
        null -> mutableListOf<Any?>()
        is MutableList<*> -> dependencyListNode.toMutableList()
        else -> error("snapshot-dependencies.snapshot-dependency must be an array")
    }

    dependencyList.add(newDependency)
    dependenciesMap["snapshot-dependency"] = dependencyList

    val currentDependencyCount = (dependenciesMap["count"] as? Int) ?: 0
    dependenciesMap["count"] = currentDependencyCount + 1

    build["snapshot-dependencies"] = dependenciesMap
}

fun chainBuilds(
    builds: List<MutableMap<String, Any>>,
) {
    var previousBuild: MutableMap<String, Any>? = null
    for (build in builds) {
        previousBuild?.let { nonNullPreviousBuild ->
            setDependency(build, nonNullPreviousBuild)
        }
        previousBuild = build
    }
}
