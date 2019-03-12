package com.lykke.trade.volume.monitoring.service.spring

import com.lykke.me.subscriber.incoming.events.ExecutionEvent
import com.lykke.me.subscriber.incoming.events.proto.MeProtoEvent
import com.lykke.me.subscriber.rabbitmq.MeRabbitMqProtoEventListener
import com.lykke.trade.volume.monitoring.service.config.Config
import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.holder.AssetPairsHolder
import com.lykke.trade.volume.monitoring.service.holder.AssetsHolder
import com.lykke.trade.volume.monitoring.service.holder.PricesHolder
import com.lykke.trade.volume.monitoring.service.holder.impl.AssetPairsHolderImpl
import com.lykke.trade.volume.monitoring.service.holder.impl.AssetsHolderImpl
import com.lykke.trade.volume.monitoring.service.holder.impl.PricesHolderImpl
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
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

@Configuration
class IncomingEventProcessConfig {

    @Bean
    fun executionEventQueue(): BlockingQueue<ExecutionEvent> = LinkedBlockingQueue<ExecutionEvent>()

    @Bean
    fun tradeVolumesQueue(): BlockingQueue<EventTradeVolumesWrapper> = LinkedBlockingQueue<EventTradeVolumesWrapper>()

    @Bean
    fun assetsHolder(): AssetsHolder {
        return AssetsHolderImpl()
    }

    @Bean
    fun assetPairsHolder(): AssetPairsHolder {
        return AssetPairsHolderImpl()
    }

    @Bean
    fun pricesHolder(): PricesHolder {
        return PricesHolderImpl()
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