package com.lykke.trade.volume.monitoring.service.loader

import com.lykke.trade.volume.monitoring.service.entity.Asset

interface AssetsLoader {
    fun loadAssetsByIdMap(): Map<String, Asset>
    fun loadAsset(assetId: String): Asset?
}