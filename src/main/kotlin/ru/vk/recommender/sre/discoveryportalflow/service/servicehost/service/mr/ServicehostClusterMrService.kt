package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.service.mr

import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.client.ServicehostAdminClient
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.context.ServicehostTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostClusterInitializationResult
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.service.cluster.ServicehostClusterNameResolver
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.service.path.ServicehostPathResolver
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer.ServicehostApiPayloadWriter

class ServicehostClusterMrService(
    private val servicehostApiPayloadWriter: ServicehostApiPayloadWriter,
    private val servicehostAdminClient: ServicehostAdminClient,
) {

    fun createClusterMrIfNeeded(taskContext: ServicehostTaskContext): ServicehostClusterInitializationResult {
        val recommenderName = taskContext.recommenderName
        val clusterNames = ServicehostClusterNameResolver.resolveClusterNames(
            recommenderName = recommenderName,
            servicehostClusterName = taskContext.servicehostClusterName,
        )
        val servicehostRootDirectory = ServicehostPathResolver.resolveServicehostRootDirectory(
            workspaceRoot = taskContext.workspaceRoot,
            recommenderName = recommenderName,
        )

        val clusterRequestPayload = servicehostApiPayloadWriter.writeClusterRequestPayload(
            taskContext = taskContext,
            servicehostRootDirectory = servicehostRootDirectory,
            normalizedClusterName = clusterNames.normalizedClusterName,
        )

        return servicehostAdminClient.initializeCluster(
            fullClusterName = clusterNames.fullClusterName,
            clusterRequestPayload = clusterRequestPayload,
        )
    }
}
