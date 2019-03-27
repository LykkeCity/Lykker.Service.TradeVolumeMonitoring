package com.lykke.trade.volume.monitoring.service.spring

import com.lykke.trade.volume.monitoring.service.config.Config
import com.lykke.trade.volume.monitoring.service.loader.EventsLoader
import com.lykke.trade.volume.monitoring.service.loader.redis.CachedEventsLoader
import com.lykke.trade.volume.monitoring.service.loader.redis.RedisEventsLoader
import com.lykke.trade.volume.monitoring.service.persistence.redis.RedisPersistenceManager
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import java.util.concurrent.TimeUnit

@Configuration
class PersistenceConfig {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PersistenceConfig::class.java.name)
    }

    private lateinit var cachedEventsLoader: CachedEventsLoader

    @Bean
    fun redisPersistenceManager(config: Config): RedisPersistenceManager {
        val redisConfig = config.tradeVolumeConfig.redis
        val tradeVolumeCacheConfig = config.tradeVolumeConfig.tradeVolumeCacheConfig
        val ttlMs = Math.max(tradeVolumeCacheConfig.volumePeriod * tradeVolumeCacheConfig.expiryRatio, TimeUnit.HOURS.toMillis(2))
        return RedisPersistenceManager(redisConfig, TimeUnit.MILLISECONDS.toSeconds(ttlMs).toInt())
    }

    @Bean
    fun eventsLoader(config: Config): EventsLoader {
        val cachedEventsLoader = CachedEventsLoader(RedisEventsLoader(config.tradeVolumeConfig.redis))
        this.cachedEventsLoader = cachedEventsLoader
        return cachedEventsLoader
    }

    @EventListener
    fun handleContextRefresh(event: ContextRefreshedEvent) {
        clearLoaderCache()
    }

    private fun clearLoaderCache() {
        cachedEventsLoader.clear()
        LOGGER.info("Cleaned cachedEventsLoader")
    }
}