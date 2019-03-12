package com.lykke.trade.volume.monitoring.service.holder.impl

import com.lykke.trade.volume.monitoring.service.entity.Asset
import com.lykke.trade.volume.monitoring.service.holder.AssetsHolder

class AssetsHolderImpl : AssetsHolder {
    override fun getAsset(assetId: String): Asset? {
        // todo
        return Asset(assetId, 3)
    }
}