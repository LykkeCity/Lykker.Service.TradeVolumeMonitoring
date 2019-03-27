package com.lykke.trade.volume.monitoring.service.persistence.redis

import com.lykke.trade.volume.monitoring.service.config.RedisConfig
import com.lykke.trade.volume.monitoring.service.entity.EventPersistenceData
import com.lykke.trade.volume.monitoring.service.persistence.PersistenceManager
import com.lykke.trade.volume.monitoring.service.persistence.redis.utils.RedisUtils
import com.lykke.trade.volume.monitoring.service.process.EventProcessLoggerFactory
import org.nustaq.serialization.FSTConfiguration
import redis.clients.jedis.Jedis
import redis.clients.jedis.exceptions.JedisException
import java.text.SimpleDateFormat

class RedisPersistenceManager(private val redisConfig: RedisConfig,
                              private val ttl: Int) : PersistenceManager {

    companion object {
        private val LOGGER = EventProcessLoggerFactory.getLogger(RedisPersistenceManager::class.java.name)
        private val DATE_FORMAT = SimpleDateFormat("yyyyMMddHH")

        const val PREFIX = "event:"
        val fstConfiguration: FSTConfiguration = FSTConfiguration.createJsonConfiguration()
    }

    init {
        LOGGER.info(null, "Initialized with config: $redisConfig, ttl: $ttl sec")
    }

    private val jedisThreadLocal = ThreadLocal<Jedis>()

    override fun persist(eventPersistenceData: EventPersistenceData) {
        try {
            persist(getJedis(), eventPersistenceData)
        } catch (e: JedisException) {
            LOGGER.error(eventPersistenceData.sequenceNumber, "Unable to persist data (${e.message}), retry...")
            jedisThreadLocal.set(null)
            persist(getJedis(), eventPersistenceData)
            LOGGER.info(eventPersistenceData.sequenceNumber, "Successful persistence retry")
        }
    }

    private fun persist(jedis: Jedis, eventPersistenceData: EventPersistenceData) {
        jedis.multi().use { transaction ->
            transaction.select(redisConfig.databaseIndex)
            RedisUtils.performAtomicSaveSetExpire(transaction,
                    getKey(eventPersistenceData),
                    fstConfiguration.asJsonString(eventPersistenceData), ttl)
            transaction.exec()
        }
    }

    private fun getJedis(): Jedis {
        var jedis = jedisThreadLocal.get()
        if (jedis == null) {
            jedis = openRedisConnection()
            jedisThreadLocal.set(jedis)
        }
        return jedis
    }

    private fun openRedisConnection(): Jedis {
        val jedis = RedisUtils.openRedisConnection(redisConfig)
        LOGGER.info(null, "Created redis connection")
        return jedis
    }

    private fun getKey(eventPersistenceData: EventPersistenceData): String {
        return PREFIX + DATE_FORMAT.format(eventPersistenceData.timestamp)
    }
}
