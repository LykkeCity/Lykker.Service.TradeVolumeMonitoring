package com.lykke.trade.volume.monitoring.service.holder.impl

import com.lykke.trade.volume.monitoring.service.holder.PricesHolder
import java.math.BigDecimal

class PricesHolderImpl : PricesHolder {
    override fun getPrice(assetPairId: String): BigDecimal? {
        // todo
        return BigDecimal(2.5)
    }
}