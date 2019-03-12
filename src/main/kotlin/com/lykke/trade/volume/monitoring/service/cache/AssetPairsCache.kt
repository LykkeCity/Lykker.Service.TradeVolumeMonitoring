package com.lykke.trade.volume.monitoring.service.cache

import com.lykke.trade.volume.monitoring.service.entity.AssetPair

interface AssetPairsCache: DataCache {
    fun getAssetPair(assetId1: String, assetId2: String): AssetPair?
}