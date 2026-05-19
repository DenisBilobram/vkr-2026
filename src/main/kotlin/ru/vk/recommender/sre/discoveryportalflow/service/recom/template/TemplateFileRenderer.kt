package ru.vk.recommender.sre.discoveryportalflow.service.recom.template

import org.springframework.core.io.ClassPathResource
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

object TemplateFileRenderer {

    private val cache = ConcurrentHashMap<String, String>()

    fun render(path: String, values: Map<String, String>): String {
        var content = read(path)
        values.forEach { (key, value) ->
            content = content.replace("{{${key}}}", value)
        }
        return content
    }

    fun read(path: String): String {
        return cache.computeIfAbsent(path) { templatePath ->
            val resource = ClassPathResource(templatePath)
            require(resource.exists()) { "Template not found: $templatePath" }
            resource.inputStream.use { input ->
                String(input.readAllBytes(), StandardCharsets.UTF_8)
            }
        }
    }

    fun exists(path: String): Boolean {
        return ClassPathResource(path).exists()
    }
}
