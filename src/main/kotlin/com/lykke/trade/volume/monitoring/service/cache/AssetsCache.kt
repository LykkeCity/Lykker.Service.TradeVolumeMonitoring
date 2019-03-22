package com.lykke.trade.volume.monitoring.service.cache

import com.lykke.trade.volume.monitoring.service.entity.Asset

interface AssetsCache: DataCache {
    fun getAsset(assetId: String): Asset?
}