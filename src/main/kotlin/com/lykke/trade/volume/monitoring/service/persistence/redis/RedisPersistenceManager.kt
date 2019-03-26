package com.lykke.trade.volume.monitoring.service.persistence.redis

import com.lykke.trade.volume.monitoring.service.config.RedisConfig
import com.lykke.trade.volume.monitoring.service.entity.PersistenceData
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
        private const val PREFIX = "event"
        private const val SEPARATOR = ":"
    }

    private val jedisThreadLocal = ThreadLocal<Jedis>()
    private var conf = FSTConfiguration.createJsonConfiguration()

    override fun persist(data: PersistenceData) {
        try {
            persist(getJedis(), data)
        } catch (e: JedisException) {
            LOGGER.error(data.eventSequenceNumber, "Unable to persist data (${e.message}), retry...")
            jedisThreadLocal.set(null)
            persist(getJedis(), data)
            LOGGER.info(data.eventSequenceNumber, "Successful persistence retry")
        }
    }

    private fun persist(jedis: Jedis, data: PersistenceData) {
        jedis.multi().use { transaction ->
            transaction.select(redisConfig.databaseIndex)
            RedisUtils.performAtomicSaveSetExpire(transaction, getKey(data), conf.asJsonString(data), ttl)
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

    private fun getKey(data: PersistenceData): String {
        return PREFIX + SEPARATOR + DATE_FORMAT.format(data.timestamp)
    }
}
