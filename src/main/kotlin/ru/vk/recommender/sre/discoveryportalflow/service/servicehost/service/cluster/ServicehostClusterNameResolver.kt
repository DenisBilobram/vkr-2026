package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.service.cluster

import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model.ServicehostClusterNames

class ServicehostClusterNameResolver {

    companion object {

        private const val MAX_SERVICEHOST_CLUSTER_NAME_LENGTH = 9
        private const val SERVICEHOST_PROJECT_PREFIX = "public"
        private const val SERVICEHOST_ENVIRONMENT_SUFFIX = "prod"

        fun resolveClusterNames(
            recommenderName: String,
            servicehostClusterName: String?,
        ): ServicehostClusterNames {
            val rawClusterName = servicehostClusterName ?: recommenderName
            val normalizedClusterName = rawClusterName
                .replace("public-", "")
                .replace("-prod", "")

            require(normalizedClusterName.length <= MAX_SERVICEHOST_CLUSTER_NAME_LENGTH) {
                "Servicehost cluster name must be <= $MAX_SERVICEHOST_CLUSTER_NAME_LENGTH symbols after normalization, got '$normalizedClusterName'"
            }

            return ServicehostClusterNames(
                normalizedClusterName = normalizedClusterName,
                fullClusterName = "$SERVICEHOST_PROJECT_PREFIX-$normalizedClusterName-$SERVICEHOST_ENVIRONMENT_SUFFIX",
            )
        }
    }
}
