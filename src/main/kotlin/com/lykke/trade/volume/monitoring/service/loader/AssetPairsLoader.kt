package com.lykke.trade.volume.monitoring.service.loader

import com.lykke.trade.volume.monitoring.service.entity.AssetPair

interface AssetPairsLoader {
    fun loadAssetPairsByIdMap(): Map<String, AssetPair>
}