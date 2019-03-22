package com.lykke.trade.volume.monitoring.service.holder.impl

import com.lykke.trade.volume.monitoring.service.cache.AssetPairsCache
import com.lykke.trade.volume.monitoring.service.entity.AssetPair
import com.lykke.trade.volume.monitoring.service.holder.AssetPairsHolder

class AssetPairsHolderImpl(private val assetPairsCache: AssetPairsCache) : AssetPairsHolder {

    override fun getAssetPair(assetId1: String, assetId2: String): AssetPair? {
        return assetPairsCache.getAssetPair(assetId1, assetId2)
    }
}