package com.lykke.trade.volume.monitoring.service.loader.redis

import com.lykke.trade.volume.monitoring.service.config.RedisConfig
import com.lykke.trade.volume.monitoring.service.entity.EventPersistenceData
import com.lykke.trade.volume.monitoring.service.loader.EventsLoader
import com.lykke.trade.volume.monitoring.service.persistence.redis.RedisPersistenceManager
import com.lykke.trade.volume.monitoring.service.persistence.redis.utils.RedisUtils
import org.slf4j.LoggerFactory

class RedisEventsLoader(private val redisConfig: RedisConfig) : EventsLoader {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RedisEventsLoader::class.java.name)
    }

    override fun loadEvents(): List<EventPersistenceData> {
        RedisUtils.openRedisConnection(redisConfig).use { jedis ->
            jedis.select(redisConfig.databaseIndex)
            val keys = jedis.keys("${RedisPersistenceManager.PREFIX}*")
            val result = keys
                    .flatMap { jedis.lrange(it, 0, -1) }
                    .map { RedisPersistenceManager.fstConfiguration.asObject(it.toByteArray()) as EventPersistenceData }
            LOGGER.info("Loaded ${result.size} events from redis")
            return result
        }

    }
}