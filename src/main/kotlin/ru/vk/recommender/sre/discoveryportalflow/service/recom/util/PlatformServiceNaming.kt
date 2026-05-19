package ru.vk.recommender.sre.discoveryportalflow.service.recom.util

import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime

object PlatformServiceNaming {

    fun sourceDirectory(serviceName: String): String {
        return "recommender/platform/runtime/$serviceName/${serviceModuleName(serviceName)}"
    }

    fun gradleBuildCommand(serviceName: String): String {
        return "recommender:platform:runtime:$serviceName:${serviceModuleName(serviceName)}:export"
    }

    fun onecloudDirectoryName(serviceName: String): String {
        return "platform-$serviceName"
    }

    fun udfModuleName(serviceName: String): String {
        return "udf-$serviceName"
    }

    fun udfSourceDirectory(recommenderRuntime: RecommenderRuntime, udfModuleName: String): String {
        return "${recommenderRuntime.recommenderRoot}${recommenderRuntime.recommenderName}/$udfModuleName"
    }

    fun packageSegment(serviceName: String): String {
        return parseNames(serviceName).packageName
    }

    private fun serviceModuleName(serviceName: String): String {
        return "${serviceName}-service"
    }

    private fun templateSegment(serviceName: String): String {
        return serviceName.replace("-", "_")
    }
}
