package ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.recom.context.BootstrapRecomContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.ServiceQueueNames
import ru.vk.recommender.sre.discoveryportalflow.service.recom.resolver.WorkspaceResolver
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.RecommenderNames
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.parseNames
import java.nio.file.Path

private const val PUBLIC_TEST_QUEUE = "public.app.testing.recommender.prod"
private const val PUBLIC_PROD_QUEUE = "public.app.production.recommender.prod"

@JsonIgnoreProperties(ignoreUnknown = true)
data class RecommenderRuntime(
    val workspaceRoot: Path,
    val recommenderName: String,
    val serviceOwner: String,
    val additionalResponsibles: List<String> = emptyList(),
    val additionalFollowers: List<String> = emptyList(),
    val recomOneSecretId: String? = null,
    val projectOneSecretId: String? = null,
    val productId: Int? = null,
    val projectProductId: Int?,
    val vertical: String,
    val dictionaryBaseProject: String,
    val projectName: String?,
    val createMinOneCloudConfiguration: Boolean = false,
    val recommenderRoot: String,
    val teamcityProjectPrefix: String,
    val projectProductionRootQueueName: String?,
    val projectTestingRootQueueName: String?,
    val productionRootQueueName: String,
    val testingRootQueueName: String,
    val i2iProductionRootQueueName: String,
    val i2iTestingRootQueueName: String,
    val names: RecommenderNames,
    val clusterName: String,
) {
    constructor(
        stageContext: BootstrapRecomContext,
    ) : this(
        workspaceRoot = WorkspaceResolver.workspaceRoot,
        recommenderName = stageContext.recommender.recommenderName,
        serviceOwner = stageContext.recommender.serviceOwner,
        additionalResponsibles = stageContext.recommender.additionalResponsibles,
        additionalFollowers = stageContext.recommender.additionalFollowers,
        recomOneSecretId = stageContext.recomOneSecretId,
        projectOneSecretId = stageContext.projectOneSecretId,
        productId = stageContext.recommender.productId,
        projectProductId = stageContext.recommender.projectProductId,
        vertical = parseNames(stageContext.recommender.recommenderName).folderName,
        dictionaryBaseProject = stageContext.recommender.recommenderName,
        projectName = stageContext.recommender.projectName,
        createMinOneCloudConfiguration = stageContext.recommender.createMinOneCloudConfiguration,
        recommenderRoot = resolveRecommenderRoot(stageContext.recommender.projectName),
        teamcityProjectPrefix = resolveTeamcityProjectPrefix(stageContext.recommender.projectName),
        projectProductionRootQueueName = resolveProjectQueueName(stageContext.recommender.projectName, PUBLIC_PROD_QUEUE),
        projectTestingRootQueueName = resolveProjectQueueName(stageContext.recommender.projectName, PUBLIC_TEST_QUEUE),
        productionRootQueueName = resolveVerticalQueueName(
            recommenderName = stageContext.recommender.recommenderName,
            projectName = stageContext.recommender.projectName,
            queueSuffix = PUBLIC_PROD_QUEUE,
        ),
        testingRootQueueName = resolveVerticalQueueName(
            recommenderName = stageContext.recommender.recommenderName,
            projectName = stageContext.recommender.projectName,
            queueSuffix = PUBLIC_TEST_QUEUE,
        ),
        i2iProductionRootQueueName = resolveI2iQueueName(
            recommenderName = stageContext.recommender.recommenderName,
            projectName = stageContext.recommender.projectName,
            queueSuffix = PUBLIC_PROD_QUEUE,
        ),
        i2iTestingRootQueueName = resolveI2iQueueName(
            recommenderName = stageContext.recommender.recommenderName,
            projectName = stageContext.recommender.projectName,
            queueSuffix = PUBLIC_TEST_QUEUE,
        ),
        names = parseNames(stageContext.recommender.recommenderName),
        clusterName = stageContext.servicehostClusterName
            ?: stageContext.recommender.projectName
            ?: stageContext.recommender.recommenderName,
    )

    fun resolveRootQueueNames(serviceScope: ServiceScope): ServiceQueueNames {
        return when (serviceScope) {
            ServiceScope.PROJECT_SCOPED -> ServiceQueueNames(
                productionRootQueueName = requireNotNull(projectProductionRootQueueName) {
                    "Project-scoped service requires recommender.projectName and project production queue"
                },
                testingRootQueueName = requireNotNull(projectTestingRootQueueName) {
                    "Project-scoped service requires recommender.projectName and project testing queue"
                },
            )

            ServiceScope.I2I_VERTICAL_SCOPED -> ServiceQueueNames(
                productionRootQueueName = i2iProductionRootQueueName,
                testingRootQueueName = i2iTestingRootQueueName,
            )

            ServiceScope.VERTICAL_SCOPED -> ServiceQueueNames(
                productionRootQueueName = productionRootQueueName,
                testingRootQueueName = testingRootQueueName,
            )
        }
    }

    fun rootQueueGroups(): List<RecommenderRootQueueGroup> {
        return buildList {
            if (projectProductionRootQueueName != null && projectTestingRootQueueName != null) {
                add(
                    RecommenderRootQueueGroup(
                        label = "project",
                        productionRootQueueName = projectProductionRootQueueName,
                        testingRootQueueName = projectTestingRootQueueName,
                        productId = projectProductId,
                    ),
                )
            }

            add(
                RecommenderRootQueueGroup(
                    label = "recommender",
                    productionRootQueueName = productionRootQueueName,
                    testingRootQueueName = testingRootQueueName,
                    productId = productId,
                ),
            )

            add(
                RecommenderRootQueueGroup(
                    label = "i2i",
                    productionRootQueueName = i2iProductionRootQueueName,
                    productId = productId,
                ),
            )
        }
    }

    private companion object {
        fun resolveRecommenderRoot(projectName: String?): String {
            return if (projectName == null) {
                "recommender/public/"
            } else {
                "recommender/public/$projectName/"
            }
        }

        fun resolveTeamcityProjectPrefix(projectName: String?): String {
            return if (projectName == null) {
                "Public_Recommender_"
            } else {
                "Public_Recommender_${parseNames(projectName).className}"
            }
        }

        fun resolveProjectQueueName(projectName: String?, queueSuffix: String): String? {
            return projectName?.let { project -> "${project.lowercase()}.$queueSuffix" }
        }

        fun resolveVerticalQueueName(
            recommenderName: String,
            projectName: String?,
            queueSuffix: String,
        ): String {
            val projectQueueName = resolveProjectQueueName(projectName, queueSuffix)
            return if (projectQueueName == null) {
                "$recommenderName.$queueSuffix"
            } else {
                "$recommenderName.$projectQueueName"
            }
        }

        fun resolveI2iQueueName(
            recommenderName: String,
            projectName: String?,
            queueSuffix: String,
        ): String {
            val projectQueueName = resolveProjectQueueName(projectName, queueSuffix)
            return if (projectQueueName == null) {
                "$recommenderName-i2i.$queueSuffix"
            } else {
                "$recommenderName-i2i.$projectQueueName"
            }
        }
    }
}
