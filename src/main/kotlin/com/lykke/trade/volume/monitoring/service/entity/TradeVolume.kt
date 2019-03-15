package com.lykke.trade.volume.monitoring.service.entity

import java.math.BigDecimal
import java.util.Date

class TradeVolume(val walletId: String,
                  val assetId: String,
                  val volume: BigDecimal,
                  val timestamp: Date) {

    override fun toString(): String {
        return "walletId=$walletId, assetId=$assetId, volume=$volume, timestamp=$timestamp"
    }
}