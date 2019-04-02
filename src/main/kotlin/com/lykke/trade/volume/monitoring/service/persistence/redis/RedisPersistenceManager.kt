package com.lykke.trade.volume.monitoring.service.persistence.redis

import com.lykke.trade.volume.monitoring.service.config.RedisConfig
import com.lykke.trade.volume.monitoring.service.entity.EventPersistenceData
import com.lykke.trade.volume.monitoring.service.persistence.PersistenceManager
import com.lykke.trade.volume.monitoring.service.persistence.redis.utils.RedisUtils
import com.lykke.trade.volume.monitoring.service.persistence.serialization.EventPersistenceDataSerializer
import com.lykke.trade.volume.monitoring.service.process.EventProcessLoggerFactory
import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisException
import java.text.Format
import java.text.SimpleDateFormat
import kotlin.concurrent.getOrSet

class RedisPersistenceManager(private val redisConfig: RedisConfig,
                              ttl: Int,
                              private val serializer: EventPersistenceDataSerializer) : PersistenceManager {

    companion object {
        private val LOGGER = EventProcessLoggerFactory.getLogger(RedisPersistenceManager::class.java.name)
        private const val DATE_FORMAT_PATTERN = "yyyyMMddHH"
        const val PREFIX = "TradeVolumeMonitoring:event:"
    }

    private val ttlByteArray = ttl.toString().toByteArray()

    init {
        LOGGER.info(null, "Initialized with config: $redisConfig, ttl: $ttl sec")
    }

    private val jedisThreadLocal = ThreadLocal<Jedis>()
    private val dateFormatThreadLocal = ThreadLocal<Format>()

    override fun persist(eventPersistenceData: EventPersistenceData) {
        try {
            persist(getJedis(), eventPersistenceData)
        } catch (e: JedisException) {
            LOGGER.error(eventPersistenceData.sequenceNumber, "Unable to persist data (${e.message}), retry...")
            closeJedis(eventPersistenceData.sequenceNumber)
            persist(getJedis(), eventPersistenceData)
            LOGGER.info(eventPersistenceData.sequenceNumber, "Successful persistence retry")
        }
    }

    private fun persist(jedis: Jedis, eventPersistenceData: EventPersistenceData) {
        RedisUtils.performAtomicSaveSetExpire(jedis,
                getKey(eventPersistenceData),
                serializer.serialize(eventPersistenceData),
                ttlByteArray)
    }

    private fun getJedis(): Jedis {
        return jedisThreadLocal.getOrSet {
            openRedisConnection()
        }
    }

    private fun closeJedis(eventSequenceNumber: Long) {
        try {
            jedisThreadLocal.get()?.close()
        } catch (e: Exception) {
            LOGGER.error(eventSequenceNumber,
                    "Unable to close redis connection: ${e.message}",
                    e)
        } finally {
            jedisThreadLocal.set(null)
        }
    }

    private fun openRedisConnection(): Jedis {
        val jedis = RedisUtils.openRedisConnection(redisConfig)
        jedis.select(redisConfig.databaseIndex)
        LOGGER.info(null, "Created redis connection")
        return jedis
    }

    private fun getKey(eventPersistenceData: EventPersistenceData): ByteArray {
        return (PREFIX + getDateFormat().format(eventPersistenceData.timestamp)).toByteArray()
    }

    private fun getDateFormat(): Format {
        return dateFormatThreadLocal.getOrSet {
            SimpleDateFormat(DATE_FORMAT_PATTERN)
        }
    }
}
