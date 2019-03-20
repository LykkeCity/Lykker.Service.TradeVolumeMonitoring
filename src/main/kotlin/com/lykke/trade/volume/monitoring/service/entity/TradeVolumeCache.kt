package com.lykke.trade.volume.monitoring.service.entity

import java.math.BigDecimal
import java.util.*

interface TradeVolumeCache {
    fun add(eventSequenceNumber: Long,
            tradeIdx: Int,
            clientId: String,
            assetId: String,
            volume: BigDecimal,
            timestamp: Date): List<Pair<Long, BigDecimal>>
}