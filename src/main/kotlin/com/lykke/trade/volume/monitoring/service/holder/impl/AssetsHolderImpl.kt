package com.lykke.trade.volume.monitoring.service.holder.impl

import com.lykke.trade.volume.monitoring.service.cache.AssetsCache
import com.lykke.trade.volume.monitoring.service.entity.Asset
import com.lykke.trade.volume.monitoring.service.holder.AssetsHolder

class AssetsHolderImpl(private val assetsCache: AssetsCache) : AssetsHolder {
    override fun getAsset(assetId: String): Asset? {
        return assetsCache.getAsset(assetId)
    }
}