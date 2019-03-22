package com.lykke.trade.volume.monitoring.service.process

import java.math.BigDecimal

interface AssetVolumeConverter {
    fun convert(assetId: String,
                volume: BigDecimal,
                targetAssetId: String): BigDecimal
}