package com.lykke.trade.volume.monitoring.service.holder

import com.lykke.trade.volume.monitoring.service.entity.AssetPair

interface AssetPairsHolder {
    fun getAssetPair(assetId1: String, assetId2: String): AssetPair?
}