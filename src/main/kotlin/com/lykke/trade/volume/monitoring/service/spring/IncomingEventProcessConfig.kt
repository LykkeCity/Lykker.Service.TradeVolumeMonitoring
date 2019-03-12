package com.lykke.trade.volume.monitoring.service.spring

import com.lykke.me.subscriber.incoming.events.ExecutionEvent
import com.lykke.me.subscriber.incoming.events.proto.MeProtoEvent
import com.lykke.me.subscriber.rabbitmq.MeRabbitMqProtoEventListener
import com.lykke.trade.volume.monitoring.service.cache.AssetPairsCache
import com.lykke.trade.volume.monitoring.service.cache.AssetsCache
import com.lykke.trade.volume.monitoring.service.cache.CacheUpdater
import com.lykke.trade.volume.monitoring.service.cache.DataCache
import com.lykke.trade.volume.monitoring.service.cache.PricesCache
import com.lykke.trade.volume.monitoring.service.cache.impl.AssetPairsCacheImpl
import com.lykke.trade.volume.monitoring.service.cache.impl.AssetsCacheImpl
import com.lykke.trade.volume.monitoring.service.cache.impl.PricesCacheImpl
import com.lykke.trade.volume.monitoring.service.config.Config
import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.entity.Rate
import com.lykke.trade.volume.monitoring.service.holder.AssetPairsHolder
import com.lykke.trade.volume.monitoring.service.holder.AssetsHolder
import com.lykke.trade.volume.monitoring.service.holder.PricesHolder
import com.lykke.trade.volume.monitoring.service.holder.impl.AssetPairsHolderImpl
import com.lykke.trade.volume.monitoring.service.holder.impl.AssetsHolderImpl
import com.lykke.trade.volume.monitoring.service.holder.impl.PricesHolderImpl
import com.lykke.trade.volume.monitoring.service.loader.AssetPairsLoader
import com.lykke.trade.volume.monitoring.service.loader.AssetsLoader
import com.lykke.trade.volume.monitoring.service.loader.RatesLoader
import com.lykke.trade.volume.monitoring.service.loader.azure.AzureAssetPairsLoader
import com.lykke.trade.volume.monitoring.service.loader.azure.AzureAssetsLoader
import com.lykke.trade.volume.monitoring.service.process.AssetVolumeConverter
import com.lykke.trade.volume.monitoring.service.process.ExecutionEventListener
import com.lykke.trade.volume.monitoring.service.process.ExecutionEventProcessor
import com.lykke.trade.volume.monitoring.service.process.MatchingEngineEventSubscriber
import com.lykke.trade.volume.monitoring.service.process.TradeVolumesListener
import com.lykke.trade.volume.monitoring.service.process.TradeVolumesProcessor
import com.lykke.trade.volume.monitoring.service.process.impl.AssetVolumeConverterImpl
import com.lykke.trade.volume.monitoring.service.process.impl.ExecutionEventListenerImpl
import com.lykke.trade.volume.monitoring.service.process.impl.MatchingEngineExecutionEventSubscriberImpl
import com.lykke.trade.volume.monitoring.service.process.impl.ProtoExecutionEventProcessor
import com.lykke.trade.volume.monitoring.service.process.impl.TradeVolumesListenerImpl
import com.lykke.trade.volume.monitoring.service.process.impl.TradeVolumesProcessorImpl
import com.lykke.utils.notification.Listener
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

@Configuration
class IncomingEventProcessConfig {

    @Bean
    fun executionEventQueue(): BlockingQueue<ExecutionEvent> = LinkedBlockingQueue<ExecutionEvent>()

    @Bean
    fun tradeVolumesQueue(): BlockingQueue<EventTradeVolumesWrapper> = LinkedBlockingQueue<EventTradeVolumesWrapper>()

    @Bean
    fun assetsLoader(config: Config): AssetsLoader {
        return AzureAssetsLoader(config.tradeVolumeConfig.db.assetsConnString)
    }

    @Bean
    fun assetPairsLoader(config: Config): AssetPairsLoader {
        return AzureAssetPairsLoader(config.tradeVolumeConfig.db.assetPairsConnString)
    }

    @Bean
    fun ratesLoader(): RatesLoader {
        // todo
        return object : RatesLoader {
            override fun loadRatesByAssetPairIdMap(): Map<String, Rate> {
                return emptyMap()
            }

            override fun loadRate(assetPairId: String): Rate? {
                return null
            }

        }
    }

    @Bean
    fun assetsCache(assetsLoader: AssetsLoader,
                    @Value("\${application.assets.cache.update.interval}")
                    updateInterval: Long): AssetsCache {
        return AssetsCacheImpl(assetsLoader, updateInterval)
    }

    @Bean
    fun assetPairsCache(assetPairsLoader: AssetPairsLoader,
                        @Value("\${application.assetpairs.cache.update.interval}")
                        updateInterval: Long): AssetPairsCacheImpl {
        return AssetPairsCacheImpl(assetPairsLoader, updateInterval)
    }

    @Bean
    fun pricesCache(ratesLoader: RatesLoader,
                    @Value("\${application.rates.cache.update.interval}")
                    updateInterval: Long): PricesCache {
        return PricesCacheImpl(ratesLoader, updateInterval)
    }

    @Bean(initMethod = "start")
    fun cacheUpdater(dataCaches: List<DataCache>): CacheUpdater {
        return CacheUpdater(dataCaches)
    }

    @Bean
    fun assetsHolder(assetsCache: AssetsCache): AssetsHolder {
        return AssetsHolderImpl(assetsCache)
    }

    @Bean
    fun assetPairsHolder(assetPairsCache: AssetPairsCache): AssetPairsHolder {
        return AssetPairsHolderImpl(assetPairsCache)
    }

    @Bean
    fun pricesHolder(pricesCache: PricesCache): PricesHolder {
        return PricesHolderImpl(pricesCache)
    }

    @Bean(initMethod = "start")
    fun matchingEngineEventListener(config: Config): Listener<MeProtoEvent<*>> {
        return MeRabbitMqProtoEventListener(config.tradeVolumeConfig.matchingEngineRabbitMqConfigs, LinkedBlockingQueue())
    }

    @Bean(initMethod = "subscribe")
    fun matchingEngineExecutionEventSubscriber(matchingEngineEventListener: Listener<MeProtoEvent<*>>,
                                               executionEventQueue: BlockingQueue<ExecutionEvent>): MatchingEngineEventSubscriber {
        return MatchingEngineExecutionEventSubscriberImpl(matchingEngineEventListener,
                executionEventQueue)
    }

    @Bean(initMethod = "startProcessingExecutionEvents")
    fun executionEventListener(executionEventQueue: BlockingQueue<ExecutionEvent>,
                               executionEventProcessor: ExecutionEventProcessor,
                               tradeVolumesQueue: BlockingQueue<EventTradeVolumesWrapper>): ExecutionEventListener {
        return ExecutionEventListenerImpl(executionEventQueue,
                executionEventProcessor,
                tradeVolumesQueue)
    }

    @Bean
    fun executionEventProcessor(): ExecutionEventProcessor {
        return ProtoExecutionEventProcessor()
    }

    @Bean(initMethod = "startProcessingTradeVolumes")
    fun tradeVolumesListener(tradeVolumesQueue: BlockingQueue<EventTradeVolumesWrapper>,
                             tradeVolumesProcessor: TradeVolumesProcessor): TradeVolumesListener {
        return TradeVolumesListenerImpl(tradeVolumesQueue,
                tradeVolumesProcessor)
    }

    @Bean
    fun tradeVolumesProcessor(assetVolumeConverter: AssetVolumeConverter,
                              config: Config): TradeVolumesProcessor {
        return TradeVolumesProcessorImpl(config.tradeVolumeConfig.assetId,
                assetVolumeConverter)
    }

    @Bean
    fun assetVolumeConverter(assetsHolder: AssetsHolder,
                             assetPairsHolder: AssetPairsHolder,
                             pricesHolder: PricesHolder): AssetVolumeConverter {
        return AssetVolumeConverterImpl(assetsHolder,
                assetPairsHolder,
                pricesHolder)
    }

}