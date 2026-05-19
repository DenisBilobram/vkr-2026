package ru.vk.recommender.sre.discoveryportalflow.service.toggles.util

object TogglesTenantNameResolver {

    fun resolveOnlineTenantName(
        recommenderName: String,
    ): String = recommenderName

    fun resolveVerticalOfflineTenantName(
        recommenderName: String,
    ): String = resolveOfflineTenantName(recommenderName)

    fun resolveProjectOfflineTenantName(
        projectName: String,
    ): String = resolveOfflineTenantName(projectName)

    fun resolveOfflineTenantName(
        namespace: String,
    ): String = "offline-$namespace"
}
