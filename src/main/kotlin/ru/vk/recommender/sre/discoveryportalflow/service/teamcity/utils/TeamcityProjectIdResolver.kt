package ru.vk.recommender.sre.discoveryportalflow.service.teamcity.utils

import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ProjectRecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.parseNames

object TeamcityProjectIdResolver {

    fun resolveProjectProjectId(
        projectRecommenderRuntime: ProjectRecommenderRuntime,
    ): String = "Public_Recommender_${parseNames(projectRecommenderRuntime.projectName).className}"

    fun resolveVerticalProjectId(
        recommenderRuntime: RecommenderRuntime,
    ): String = "${recommenderRuntime.teamcityProjectPrefix}_${recommenderRuntime.names.className}"

    fun resolveServiceProjectId(
        serviceRuntime: ServiceRuntime,
    ): String = serviceRuntime.teamcityProject
}
