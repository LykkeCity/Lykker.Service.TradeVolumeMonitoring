package com.lykke.trade.volume.monitoring.service.config

import com.lykke.me.subscriber.config.RabbitMqConfig

class TradeVolumeConfig(val tradeVolumeCacheConfig: TradeVolumeCacheConfig,
                        val maxTradeVolume: Long,
                        val assetId: String,
                        val matchingEngineRabbitMqConfigs: Set<RabbitMqConfig>,
                        val db: DbConfig)