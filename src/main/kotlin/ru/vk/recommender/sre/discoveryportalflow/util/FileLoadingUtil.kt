package ru.vk.recommender.sre.discoveryportalflow.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

object FileLoadingUtil {

    private val jsonMapper: ObjectMapper =
        ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    @JvmStatic
    fun readIndexFile(indexFilePath: String): List<String> =
        readJson(indexFilePath, object : TypeReference<List<String>>() {})

    @JvmStatic
    fun <T> readJson(resourcePath: String, type: TypeReference<T>): T =
        read(resourcePath) { input -> jsonMapper.readValue(input, type) }

    private inline fun <T> read(resourcePath: String, crossinline reader: (java.io.InputStream) -> T): T {
        val input = Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")

        return input.use { stream ->
            try {
                reader(stream)
            } catch (e: MismatchedInputException) {
                throw IllegalArgumentException(buildMissingFieldMessage(resourcePath, e))
            } catch (e: JsonProcessingException) {
                throw IllegalArgumentException("Некорректный JSON в '$resourcePath': ${e.originalMessage}")
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to read resource '$resourcePath': ${e.message}")
            }
        }
    }

    private fun buildMissingFieldMessage(resourcePath: String, e: MismatchedInputException): String {

        val path = e.path.orEmpty()

        val missingField = path.lastOrNull()?.fieldName ?: "<unknown>"

        val parentPath = path
            .dropLast(1)
            .mapNotNull { ref ->
                when {
                    ref.fieldName != null -> ref.fieldName
                    ref.index >= 0 -> "[${ref.index}]"
                    else -> null
                }
            }
            .joinToString(".")

        val fullPath = if (parentPath.isBlank()) missingField else "$parentPath.$missingField"

        return "В ресурсе '$resourcePath' отсутствует обязательное поле '$missingField' по пути '$fullPath'"
    }
}
