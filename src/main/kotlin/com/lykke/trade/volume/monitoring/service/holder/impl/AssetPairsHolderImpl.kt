package com.lykke.trade.volume.monitoring.service.holder.impl

import com.lykke.trade.volume.monitoring.service.entity.AssetPair
import com.lykke.trade.volume.monitoring.service.holder.AssetPairsHolder

class AssetPairsHolderImpl : AssetPairsHolder {

    override fun getAssetPair(assetId1: String, assetId2: String): AssetPair? {
        // todo
        return AssetPair("$assetId1-$assetId2", assetId1, assetId2, 5)
    }
}