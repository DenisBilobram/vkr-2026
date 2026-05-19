package ru.vk.recommender.sre.discoveryportalflow.service.pms.service.submit

import ru.vk.recommender.sre.discoveryportalflow.service.pms.context.ServicePmsTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.template.TemplateFileRenderer

object YtProxyPmsConfigBuilder {

    fun build(taskContext: ServicePmsTaskContext): String {
        val projectName = (taskContext.projectName ?: taskContext.recommenderName)
            .replace("-", "")

        return TemplateFileRenderer.render(
            TEMPLATE_PATH,
            mapOf(
                "RECOM_NAME" to taskContext.recommenderName,
                "RECOM_NAME_FOLDER" to taskContext.recommenderFolderName,
                "PROJECT_NAME" to projectName,
            ),
        )
    }

    private const val TEMPLATE_PATH = "templates/genericrecom/pms/yt_proxy_app_config.yaml"
}
