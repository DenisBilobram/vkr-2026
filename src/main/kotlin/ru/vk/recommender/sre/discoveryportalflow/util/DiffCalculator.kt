package ru.vk.recommender.sre.discoveryportalflow.util

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

data class ComparedValues(val current: Any?, val previous: Any?)

object DiffCalculator {

    /**
     * Вычисляет для каждой версии объекта изменения относительно самой свежей версии.
     *
     * @param versionsById словарь id -> data‑объект
     * @param filterOutFieldNames список полей в корне spec, для которых не нужно считать diff
     * @return LinkedHashMap с ключами id, отсортированными по убыванию,
     *         и значениями — картой изменений: имя поля -> (значение в latest, значение в данной версии)
     */
    inline fun <reified T : Any> fromLatest(
        versionsById: Map<Long, T>,
        filterOutFieldNames: List<String> = emptyList(),
    ): LinkedHashMap<Long, Map<String, ComparedValues>> {
        if (versionsById.isEmpty()) return linkedMapOf()

        val sortedIds = versionsById.keys.sortedDescending()
        val latestId = sortedIds.first()
        val latest = versionsById.getValue(latestId)

        // Все свойства класса, кроме полей-исключений
        val fieldsToCompare = T::class.memberProperties.filterNot { filterOutFieldNames.contains(it.name) }

        return linkedMapOf<Long, Map<String, ComparedValues>>().apply {
            put(latestId, emptyMap())
            sortedIds.drop(1).forEach { id ->
                val version = versionsById.getValue(id)
                val diff = computeDiff(latest, version, fieldsToCompare)
                put(id, diff)
            }
        }
    }

    fun <T : Any> computeDiff(
        latest: T,
        version: T,
        properties: Collection<KProperty1<T, *>>
    ): Map<String, ComparedValues> {
        return properties.mapNotNull { prop ->
            val latestValue = prop.getter.call(latest)
            val versionValue = prop.getter.call(version)
            if (latestValue != versionValue) {
                prop.name to (ComparedValues(latestValue, versionValue))
            } else {
                null
            }
        }.toMap()
    }
}
