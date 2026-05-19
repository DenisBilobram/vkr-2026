package ru.vk.recommender.sre.discoveryportalflow.service.apptracer.util

object AppTracerProjectNameResolver {

    fun resolveProjectName(
        name: String,
    ): String = "Recomm-$name"
}
