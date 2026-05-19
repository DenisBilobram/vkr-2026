package ru.vk.recommender.sre.discoveryportalflow.service.hermes.service

class HermesSnapshotTypeResolver {

    fun resolveSnapshotTypeIds(
        recommenderName: String,
        baseShardsCount: Int,
    ): List<String> {
        require(baseShardsCount > 0) { "baseShardsCount must be positive" }

        return buildList {
            add("dictionary_snapshot_$recommenderName")
            add("formulae_snapshot_$recommenderName")

            repeat(baseShardsCount) { shard ->
                add("factor_hnsw_snapshot_${recommenderName}_base_shard_${shard}_chunk_0")
            }

            repeat(baseShardsCount) { shard ->
                add("factor_top_candidates_snapshot_${recommenderName}_shard_${shard}_chunk_0")
            }

            repeat(baseShardsCount) { shard ->
                add("factors_${recommenderName}_base_snapshot_2_${recommenderName}_shard_${shard}_chunk_0")
            }
        }
    }
}
