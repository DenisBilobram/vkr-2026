package ru.vk.recommender.sre.discoveryportalflow.service.recom.util

data class RecommenderNames(
    val packageName: String,
    val className: String,
    val variableName: String,
    val enumName: String,
    val folderName: String,
    val displayName: String,
)

fun parseNames(recommenderName: String): RecommenderNames {
    val parts = recommenderName.split("-").filter { it.isNotBlank() }
    val className = parts.joinToString(separator = "") { it.replaceFirstChar(Char::uppercase) }
    val displayName = parts.joinToString(separator = " ") { it.replaceFirstChar(Char::uppercase) }
    val packageName = parts.joinToString(separator = "") { it.lowercase() }
    val variableName = if (parts.isEmpty()) {
        ""
    } else {
        parts.first().lowercase() + parts.drop(1).joinToString(separator = "") { it.replaceFirstChar(Char::uppercase) }
    }
    val enumName = parts.joinToString(separator = "_") { it.uppercase() }
    val folderName = parts.joinToString(separator = "_") { it.lowercase() }

    return RecommenderNames(
        packageName = packageName,
        className = className,
        variableName = variableName,
        enumName = enumName,
        folderName = folderName,
        displayName = displayName,
    )
}
