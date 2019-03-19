package com.lykke.trade.volume.monitoring.service.config

class TradeVolumeCacheConfig(val volumePeriod: Long,
                             val expiryRatio: Int,
                             val cleanCacheInterval: Long)