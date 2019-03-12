package com.lykke.trade.volume.monitoring.service.cache

import java.math.BigDecimal

interface PricesCache: DataCache {
    fun getPrice(assetPairId: String): BigDecimal?
}