package com.lykke.trade.volume.monitoring.service.loader

import com.lykke.trade.volume.monitoring.service.entity.Rate

interface RatesLoader {
    fun loadRatesByAssetPairIdMap(): Map<String, Rate>
    fun loadRate(assetPairId: String): Rate?
}