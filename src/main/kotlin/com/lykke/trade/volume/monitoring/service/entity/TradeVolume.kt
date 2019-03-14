package com.lykke.trade.volume.monitoring.service.entity

import java.math.BigDecimal

class TradeVolume(val walletId: String,
                  val assetId: String,
                  val volume: BigDecimal) {

    override fun toString(): String {
        return "walletId=$walletId, assetId=$assetId, volume=$volume"
    }
}