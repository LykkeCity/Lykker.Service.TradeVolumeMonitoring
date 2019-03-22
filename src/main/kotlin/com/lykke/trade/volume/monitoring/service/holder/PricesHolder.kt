package com.lykke.trade.volume.monitoring.service.holder

import java.math.BigDecimal

interface PricesHolder {
    fun getPrice(assetPairId: String): BigDecimal?
}