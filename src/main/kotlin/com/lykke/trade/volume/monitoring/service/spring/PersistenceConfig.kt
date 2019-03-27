package com.lykke.trade.volume.monitoring.service.spring

import com.lykke.trade.volume.monitoring.service.config.Config
import com.lykke.trade.volume.monitoring.service.loader.EventsLoader
import com.lykke.trade.volume.monitoring.service.loader.redis.RedisEventsLoader
import com.lykke.trade.volume.monitoring.service.persistence.redis.RedisPersistenceManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class PersistenceConfig {

    @Bean
    fun redisPersistenceManager(config: Config): RedisPersistenceManager {
        val redisConfig = config.tradeVolumeConfig.redis
        val tradeVolumeCacheConfig = config.tradeVolumeConfig.tradeVolumeCacheConfig
        val ttlMs = Math.max(tradeVolumeCacheConfig.volumePeriod * tradeVolumeCacheConfig.expiryRatio, TimeUnit.HOURS.toMillis(2))
        return RedisPersistenceManager(redisConfig, TimeUnit.MILLISECONDS.toSeconds(ttlMs).toInt())
    }

    @Bean
    fun eventsLoader(config: Config): EventsLoader {
        return RedisEventsLoader(config.tradeVolumeConfig.redis)
    }
}