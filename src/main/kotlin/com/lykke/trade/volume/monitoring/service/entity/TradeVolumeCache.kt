package com.lykke.trade.volume.monitoring.service.entity

import java.math.BigDecimal
import java.util.*

interface TradeVolumeCache {
    fun add(clientId: String,
            assetId: String,
            volume: BigDecimal,
            timestamp: Date): Map<Long, BigDecimal>
}