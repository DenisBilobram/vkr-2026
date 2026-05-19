package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.renderer

object ServicehostYamlRawDumper {

    fun dumpDocuments(documents: List<Map<String, Any?>>): String {
        if (documents.isEmpty()) {
            return ""
        }

        return documents.joinToString(separator = "\n\n") { document ->
            "---\n${renderValue(document, depth = 0)}"
        }
    }

    private fun renderValue(value: Any?, depth: Int): String {
        return when (value) {
            is Map<*, *> -> renderMap(value, depth)
            is List<*> -> renderList(value, depth)
            null -> "${indent(depth)}null"
            else -> "${indent(depth)}$value"
        }
    }

    private fun renderMap(
        mapValue: Map<*, *>,
        depth: Int,
    ): String {
        return mapValue.entries.joinToString(separator = "\n") { entry ->
            renderMapEntry(entry.key.toString(), entry.value, depth)
        }
    }

    private fun renderMapEntry(
        key: String,
        value: Any?,
        depth: Int,
    ): String {
        return when (value) {
            is Map<*, *>,
            is List<*> -> "${indent(depth)}$key:\n${renderValue(value, depth + 1)}"
            null -> "${indent(depth)}$key: null"
            else -> "${indent(depth)}$key: $value"
        }
    }

    private fun renderList(
        listValue: List<*>,
        depth: Int,
    ): String {
        return listValue.joinToString(separator = "\n") { listItem ->
            renderListItem(listItem, depth)
        }
    }

    private fun renderListItem(
        listItem: Any?,
        depth: Int,
    ): String {
        return when (listItem) {
            is Map<*, *> -> renderMapListItem(listItem, depth)
            is List<*> -> "${indent(depth)}-\n${renderValue(listItem, depth + 1)}"
            null -> "${indent(depth)}- null"
            else -> "${indent(depth)}- $listItem"
        }
    }

    private fun renderMapListItem(
        mapListItem: Map<*, *>,
        depth: Int,
    ): String {
        if (mapListItem.isEmpty()) {
            return "${indent(depth)}- {}"
        }

        val normalizedEntries = mapListItem.entries.map { entry -> entry.key.toString() to entry.value }
        val firstEntry = normalizedEntries.first()
        val firstLine = renderFirstMapListItemLine(
            firstKey = firstEntry.first,
            firstValue = firstEntry.second,
            depth = depth,
        )

        val remainingEntries = normalizedEntries.drop(1)
        if (remainingEntries.isEmpty()) {
            return firstLine
        }

        val remainingMap = linkedMapOf<String, Any?>().apply {
            remainingEntries.forEach { (entryKey, entryValue) ->
                put(entryKey, entryValue)
            }
        }

        return "$firstLine\n${renderValue(remainingMap, depth + 1)}"
    }

    private fun renderFirstMapListItemLine(
        firstKey: String,
        firstValue: Any?,
        depth: Int,
    ): String {
        return when (firstValue) {
            is Map<*, *>,
            is List<*> -> "${indent(depth)}- $firstKey:\n${renderValue(firstValue, depth + 2)}"
            null -> "${indent(depth)}- $firstKey: null"
            else -> "${indent(depth)}- $firstKey: $firstValue"
        }
    }

    private fun indent(depth: Int): String {
        return INDENT.repeat(depth)
    }

    private const val INDENT = "  "
}
