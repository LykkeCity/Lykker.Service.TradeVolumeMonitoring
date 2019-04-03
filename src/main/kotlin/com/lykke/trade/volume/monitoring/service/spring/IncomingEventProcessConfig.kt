package com.lykke.trade.volume.monitoring.service.spring

import com.lykke.client.accounts.ClientAccountsCache
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
import com.lykke.trade.volume.monitoring.service.entity.AssetDictionarySource
import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.cache.TradeVolumeCache
import com.lykke.trade.volume.monitoring.service.holder.AssetPairsHolder
import com.lykke.trade.volume.monitoring.service.holder.AssetsHolder
import com.lykke.trade.volume.monitoring.service.holder.PricesHolder
import com.lykke.trade.volume.monitoring.service.holder.impl.AssetPairsHolderImpl
import com.lykke.trade.volume.monitoring.service.holder.impl.AssetsHolderImpl
import com.lykke.trade.volume.monitoring.service.holder.impl.PricesHolderImpl
import com.lykke.trade.volume.monitoring.service.loader.AssetPairsLoader
import com.lykke.trade.volume.monitoring.service.loader.AssetsLoader
import com.lykke.trade.volume.monitoring.service.loader.EventsLoader
import com.lykke.trade.volume.monitoring.service.loader.RatesLoader
import com.lykke.trade.volume.monitoring.service.loader.azure.AzureAssetPairsLoader
import com.lykke.trade.volume.monitoring.service.loader.azure.AzureAssetsLoader
import com.lykke.trade.volume.monitoring.service.loader.http.PublicApiAssetPairsLoader
import com.lykke.trade.volume.monitoring.service.loader.http.PublicApiAssetsLoader
import com.lykke.trade.volume.monitoring.service.loader.http.PublicApiRatesLoader
import com.lykke.trade.volume.monitoring.service.notification.NotificationService
import com.lykke.trade.volume.monitoring.service.persistence.PersistenceManager
import com.lykke.trade.volume.monitoring.service.process.AssetVolumeConverter
import com.lykke.trade.volume.monitoring.service.process.EventDeduplicationService
import com.lykke.trade.volume.monitoring.service.process.ExecutionEventProcessor
import com.lykke.trade.volume.monitoring.service.process.MatchingEngineEventSubscriber
import com.lykke.trade.volume.monitoring.service.process.TradeVolumesProcessor
import com.lykke.trade.volume.monitoring.service.process.impl.AssetVolumeConverterImpl
import com.lykke.trade.volume.monitoring.service.process.impl.EventDeduplicationServiceImpl
import com.lykke.trade.volume.monitoring.service.process.impl.ExecutionEventListenerImpl
import com.lykke.trade.volume.monitoring.service.process.impl.MatchingEngineExecutionEventSubscriberImpl
import com.lykke.trade.volume.monitoring.service.process.impl.ProtoExecutionEventProcessor
import com.lykke.trade.volume.monitoring.service.process.impl.TradeVolumesListenerImpl
import com.lykke.trade.volume.monitoring.service.process.impl.TradeVolumesProcessorImpl
import com.lykke.trade.volume.monitoring.service.spring.executor.ThreadPoolExecutorWithLogExceptionSupport
import com.lykke.utils.notification.Listener
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.config.ConstructorArgumentValues
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.TimeUnit

@Configuration
class IncomingEventProcessConfig : BeanFactoryPostProcessor {

    @Bean
    fun applicationThreadPool(@Value("\${concurrent.application.pool.core.size}") corePoolSize: Int,
                             @Value("\${concurrent.application.pool.max.size}") maxPoolSize: Int): TaskExecutor {
        return ConcurrentTaskExecutor(ThreadPoolExecutorWithLogExceptionSupport(corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                SynchronousQueue<Runnable>(),
                "application-pool-thread-%d"))
     }

    @Bean
    fun incomingEventProcessThreadPool(config: Config): TaskExecutor {
        val size = config.tradeVolumeConfig.threadsNumber * 2
        return ConcurrentTaskExecutor(ThreadPoolExecutorWithLogExceptionSupport(size,
                size,
                60L,
                TimeUnit.SECONDS,
                SynchronousQueue<Runnable>(),
                "in-event-process-%d"))
    }

    @Bean
    fun executionEventQueue(): BlockingQueue<ExecutionEvent> = LinkedBlockingQueue<ExecutionEvent>()

    @Bean
    fun tradeVolumesQueue(): BlockingQueue<EventTradeVolumesWrapper> = LinkedBlockingQueue<EventTradeVolumesWrapper>()

    @Bean
    fun assetsLoader(config: Config): AssetsLoader {
        return when (config.tradeVolumeConfig.assetDictionarySource) {
            AssetDictionarySource.Azure ->
                azureAssetsLoader(config.tradeVolumeConfig.azureAssetDictionaries!!.assetsConnString)
            AssetDictionarySource.PublicApi ->
                publicApiAssetsLoader(config.tradeVolumeConfig.publicApiUrl)
        }
    }

    private fun azureAssetsLoader(connectionString: String): AzureAssetsLoader {
        return AzureAssetsLoader(connectionString)
    }

    private fun publicApiAssetsLoader(publicApiUrl: String): PublicApiAssetsLoader {
        return PublicApiAssetsLoader(publicApiUrl)
    }

    @Bean
    fun assetPairsLoader(config: Config): AssetPairsLoader {
        return when (config.tradeVolumeConfig.assetDictionarySource) {
            AssetDictionarySource.Azure ->
                azureAssetPairsLoader(config.tradeVolumeConfig.azureAssetDictionaries!!.assetPairsConnString)
            AssetDictionarySource.PublicApi ->
                publicApiAssetPairsLoader(config.tradeVolumeConfig.publicApiUrl)
        }
    }

    private fun azureAssetPairsLoader(connectionString: String): AzureAssetPairsLoader {
        return AzureAssetPairsLoader(connectionString)
    }

    private fun publicApiAssetPairsLoader(publicApiUrl: String): PublicApiAssetPairsLoader {
        return PublicApiAssetPairsLoader(publicApiUrl)
    }

    @Bean
    fun ratesLoader(config: Config): RatesLoader {
        return PublicApiRatesLoader(config.tradeVolumeConfig.publicApiUrl)
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
    fun cacheUpdater(dataCaches: List<DataCache>,
                     taskScheduler: TaskScheduler): CacheUpdater {
        return CacheUpdater(dataCaches, taskScheduler)
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
    fun matchingEngineExecutionEventSubscriber(eventDeduplicationService: EventDeduplicationService,
                                               matchingEngineEventListener: Listener<MeProtoEvent<*>>,
                                               executionEventQueue: BlockingQueue<ExecutionEvent>): MatchingEngineEventSubscriber {
        return MatchingEngineExecutionEventSubscriberImpl(eventDeduplicationService,
                matchingEngineEventListener,
                executionEventQueue)
    }

    @Bean
    fun executionEventProcessor(): ExecutionEventProcessor {
        return ProtoExecutionEventProcessor()
    }

    @Bean(initMethod = "init")
    fun eventDeduplicationService(eventsLoader: EventsLoader,
                                  taskScheduler: TaskScheduler,
                                  config: Config): EventDeduplicationService {
        val tradeVolumeCacheConfig = config.tradeVolumeConfig.tradeVolumeCacheConfig
        return EventDeduplicationServiceImpl(eventsLoader,
                tradeVolumeCacheConfig.volumePeriod * tradeVolumeCacheConfig.expiryRatio,
                taskScheduler)
    }

    @Bean
    fun tradeVolumesProcessor(assetVolumeConverter: AssetVolumeConverter,
                              config: Config,
                              persistenceManager: PersistenceManager,
                              tradeVolumeCache: TradeVolumeCache,
                              notificationService: NotificationService,
                              clientAccountsCache: ClientAccountsCache): TradeVolumesProcessor {
        return TradeVolumesProcessorImpl(config.tradeVolumeConfig.assetId,
                config.tradeVolumeConfig.crossAssetIds ?: emptySet(),
                assetVolumeConverter,
                persistenceManager,
                tradeVolumeCache,
                config.tradeVolumeConfig.maxVolume,
                notificationService,
                clientAccountsCache)
    }

    @Bean
    fun assetVolumeConverter(assetsHolder: AssetsHolder,
                             assetPairsHolder: AssetPairsHolder,
                             pricesHolder: PricesHolder): AssetVolumeConverter {
        return AssetVolumeConverterImpl(assetsHolder,
                assetPairsHolder,
                pricesHolder)
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val threadsNumber = beanFactory.getBean(Config::class.java).tradeVolumeConfig.threadsNumber
        if (threadsNumber <= 0) {
            throw IllegalStateException("configuration value 'threadsNumber' must be positive, actual value: $threadsNumber")
        }
        registerExecutionEventListeners(threadsNumber, beanFactory as DefaultListableBeanFactory)
        registerTradeVolumesListeners(threadsNumber, beanFactory)
    }

    private fun registerExecutionEventListeners(number: Int, factory: DefaultListableBeanFactory) {
        (1..number).forEach {
            registerExecutionEventListener(it, factory)
        }
    }

    private fun registerExecutionEventListener(index: Int, factory: DefaultListableBeanFactory) {
        val beanDefinition = RootBeanDefinition(ExecutionEventListenerImpl::class.java)
        beanDefinition.initMethodName = ExecutionEventListenerImpl::startProcessingExecutionEvents.name

        val constructorArgumentValues = ConstructorArgumentValues()
        constructorArgumentValues.addIndexedArgumentValue(0, index)
        constructorArgumentValues.addIndexedArgumentValue(1, factory.getBean("incomingEventProcessThreadPool"))

        beanDefinition.constructorArgumentValues = constructorArgumentValues
        factory.registerBeanDefinition("executionEventListener$index", beanDefinition)
    }

    private fun registerTradeVolumesListeners(number: Int, factory: DefaultListableBeanFactory) {
        (1..number).forEach {
            registerTradeVolumesListener(it, factory)
        }
    }

    private fun registerTradeVolumesListener(index: Int, factory: DefaultListableBeanFactory) {
        val beanDefinition = RootBeanDefinition(TradeVolumesListenerImpl::class.java)
        beanDefinition.initMethodName = TradeVolumesListenerImpl::startProcessingTradeVolumes.name

        val constructorArgumentValues = ConstructorArgumentValues()
        constructorArgumentValues.addIndexedArgumentValue(0, index)
        constructorArgumentValues.addIndexedArgumentValue(1, factory.getBean("incomingEventProcessThreadPool"))

        beanDefinition.constructorArgumentValues = constructorArgumentValues
        factory.registerBeanDefinition("tradeVolumesListener$index", beanDefinition)
    }

}