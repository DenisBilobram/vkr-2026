package ru.vk.recommender.sre.discoveryportalflow.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object CopyUtil {

    val MAPPER: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun <T : Any> copy(src: T?): T? {
        if (src == null) return null
        @Suppress("UNCHECKED_CAST")
        return MAPPER.convertValue(src, src.javaClass) as T
    }

    fun <T : Any> deepCopy(src: T): T {
        return try {
            val json = MAPPER.writeValueAsBytes(src)
            @Suppress("UNCHECKED_CAST")
            MAPPER.readValue(json, src.javaClass) as T
        } catch (e: Exception) {
            throw RuntimeException("Failed to deep copy ${src.javaClass.name}", e)
        }
    }
}
