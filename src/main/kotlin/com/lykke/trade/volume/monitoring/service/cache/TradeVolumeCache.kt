package com.lykke.trade.volume.monitoring.service.cache

import com.lykke.trade.volume.monitoring.service.entity.ClientTradeVolume
import java.math.BigDecimal
import java.util.*

interface TradeVolumeCache {
    fun add(eventSequenceNumber: Long,
            tradeIdx: Int,
            clientId: String,
            assetId: String,
            volume: BigDecimal,
            timestamp: Date): List<Pair<Long, BigDecimal>>

    fun getTradeVolumeForLastPeriod(clientId: String, assetId: String): ClientTradeVolume?

    fun getTradeVolumesForLastPeriod(clientId: String): List<ClientTradeVolume>
}