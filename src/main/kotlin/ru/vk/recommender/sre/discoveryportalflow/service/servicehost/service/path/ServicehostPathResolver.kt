package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.service.path

import java.nio.file.Path

object ServicehostPathResolver {

    fun resolveServicehostRootDirectory(
        workspaceRoot: Path,
        recommenderName: String,
    ): Path {
        return workspaceRoot.resolve("servicehost-conf/$recommenderName")
    }

    fun resolveServicehostClusterDirectory(
        servicehostRootDirectory: Path,
        fullClusterName: String,
    ): Path {
        return servicehostRootDirectory.resolve("projects/public/servicehost-clusters/$fullClusterName")
    }
}
