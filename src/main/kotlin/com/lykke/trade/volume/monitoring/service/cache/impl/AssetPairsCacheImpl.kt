package com.lykke.trade.volume.monitoring.service.cache.impl

import com.lykke.trade.volume.monitoring.service.cache.AssetPairsCache
import com.lykke.trade.volume.monitoring.service.entity.AssetPair
import com.lykke.trade.volume.monitoring.service.loader.AssetPairsLoader
import com.lykke.utils.logging.ThrottlingLogger

class AssetPairsCacheImpl(private val assetPairsLoader: AssetPairsLoader,
                          override val updateInterval: Long) : AssetPairsCache {

    companion object {
        private val LOGGER = ThrottlingLogger.getLogger(AssetPairsCacheImpl::class.java.name)
    }

    @Volatile
    private var assetPairsByPairKey: Map<String, AssetPair> = emptyMap()

    override fun getAssetPair(assetId1: String, assetId2: String): AssetPair? {
        return assetPairsByPairKey[pairKey(assetId1, assetId2)] ?: assetPairsByPairKey[pairKey(assetId2, assetId1)]
    }

    override fun update() {
        val assetPairsById = try {
            assetPairsLoader.loadAssetPairsByIdMap()
        } catch (e: Exception) {
            LOGGER.error("Unable to load asset pairs", e)
            return
        }

        val assetPairsByPairKey = convertToAssetPairsByPairKeyMap(assetPairsById)
        this.assetPairsByPairKey = assetPairsByPairKey
        LOGGER.debug("Loaded ${assetPairsById.size} asset pairs (grouped by pair key: ${assetPairsByPairKey.size})")
    }

    private fun convertToAssetPairsByPairKeyMap(assetPairsById: Map<String, AssetPair>): Map<String, AssetPair> {
        return assetPairsById.values
                .groupBy {
                    if (it.baseAssetId > it.quotingAssetId)
                        pairKey(it.baseAssetId, it.quotingAssetId)
                    else
                        pairKey(it.quotingAssetId, it.baseAssetId)
                }
                .mapValues {
                    if (it.value.size > 1) {
                        LOGGER.error("There are more than 1 asset pair for baseAssetId=${it.value.first().baseAssetId} " +
                                "and quotingAssetId=${it.value.first().quotingAssetId}")
                    }
                    it.value.first()
                }
    }

    private fun pairKey(assetId1: String, assetId2: String) = "${assetId1}_$assetId2"

    init {
        update()
        LOGGER.info("Loaded ${assetPairsByPairKey.size} asset pairs initially")
    }
}