package ru.vk.recommender.sre.discoveryportalflow.service.yt.model

data class YtMockTablePaths(
    val root: String,
    val tmp: String,
    val candidates: String,
    val itemFactors: String,
    val itemEmbeddings: String,
) {
    fun asStrings(): List<String> {
        return listOf(
            candidates,
            itemFactors,
            itemEmbeddings,
        )
    }
}
