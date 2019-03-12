package com.lykke.trade.volume.monitoring.service.holder.impl

import com.lykke.trade.volume.monitoring.service.cache.PricesCache
import com.lykke.trade.volume.monitoring.service.holder.PricesHolder
import java.math.BigDecimal

class PricesHolderImpl(private val pricesCache: PricesCache) : PricesHolder {
    override fun getPrice(assetPairId: String): BigDecimal? {
        return pricesCache.getPrice(assetPairId)
    }
}