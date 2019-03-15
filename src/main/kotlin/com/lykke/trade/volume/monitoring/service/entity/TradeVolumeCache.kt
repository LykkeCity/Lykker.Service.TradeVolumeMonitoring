package com.lykke.trade.volume.monitoring.service.entity

import java.math.BigDecimal
import java.util.*

interface TradeVolumeCache {
    fun clear()
    fun add(walletId: String,
            assetId: String,
            volume: BigDecimal,
            timestamp: Date): BigDecimal
    fun get(walletId: String, assetId: String): BigDecimal
}