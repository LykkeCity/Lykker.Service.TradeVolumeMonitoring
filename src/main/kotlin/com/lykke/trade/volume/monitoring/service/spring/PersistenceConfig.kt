package com.lykke.trade.volume.monitoring.service.spring

import com.lykke.trade.volume.monitoring.service.config.Config
import com.lykke.trade.volume.monitoring.service.loader.EventsLoader
import com.lykke.trade.volume.monitoring.service.loader.redis.RedisEventsLoader
import com.lykke.trade.volume.monitoring.service.persistence.redis.RedisPersistenceManager
import com.lykke.trade.volume.monitoring.service.persistence.serialization.EventPersistenceDataSerializer
import com.lykke.trade.volume.monitoring.service.persistence.serialization.fst.FstEventPersistenceDataSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class PersistenceConfig {

    companion object {
        private val MIN_TTL_MS = TimeUnit.HOURS.toMillis(2)
    }

    @Bean
    fun eventPersistenceDataSerializer(): EventPersistenceDataSerializer {
        return FstEventPersistenceDataSerializer()
    }

    @Bean
    fun redisPersistenceManager(config: Config,
                                eventPersistenceDataSerializer: EventPersistenceDataSerializer): RedisPersistenceManager {
        val redisConfig = config.tradeVolumeConfig.redis
        val tradeVolumeCacheConfig = config.tradeVolumeConfig.tradeVolumeCacheConfig
        val ttlMs = Math.max(tradeVolumeCacheConfig.volumePeriod * tradeVolumeCacheConfig.expiryRatio, MIN_TTL_MS)
        return RedisPersistenceManager(redisConfig,
                TimeUnit.MILLISECONDS.toSeconds(ttlMs).toInt(),
                eventPersistenceDataSerializer)
    }

    @Bean
    fun eventsLoader(config: Config,
                     eventPersistenceDataSerializer: EventPersistenceDataSerializer): EventsLoader {
        return RedisEventsLoader(config.tradeVolumeConfig.redis,
                eventPersistenceDataSerializer)
    }
}