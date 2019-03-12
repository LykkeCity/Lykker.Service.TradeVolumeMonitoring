package com.lykke.trade.volume.monitoring.service.entity

import java.math.BigDecimal

class Rate(val assetPairId: String,
           val bid: BigDecimal?,
           val ask: BigDecimal?)