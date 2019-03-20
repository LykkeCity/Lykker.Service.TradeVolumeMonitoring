package com.lykke.trade.volume.monitoring.service.config

import com.lykke.me.subscriber.config.RabbitMqConfig
import com.lykke.trade.volume.monitoring.service.entity.AssetDictionarySource

class TradeVolumeConfig(val tradeVolumeCacheConfig: TradeVolumeCacheConfig,
                        val maxVolume: BigDecimal,
                        val assetId: String,
                        val matchingEngineRabbitMqConfigs: Set<RabbitMqConfig>,
                        val assetDictionarySource: AssetDictionarySource,
                        val azureAssetDictionaries: AzureAssetDictionariesConfig?,
                        val publicApiUrl: String,
                        val threadsNumber: Int)