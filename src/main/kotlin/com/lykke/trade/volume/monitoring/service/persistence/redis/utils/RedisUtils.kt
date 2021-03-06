package com.lykke.trade.volume.monitoring.service.persistence.redis.utils

import com.lykke.trade.volume.monitoring.service.config.RedisConfig
import redis.clients.jedis.Jedis

class RedisUtils {

    companion object {
        private val LUA_ATOMIC_SAVE_SET_EXPIRE_SCRIPT = """local firstSave = redis.call('exists', KEYS[1])
            |redis.call('rpush', KEYS[1], ARGV[1])
            |if firstSave == 0
            |then redis.call('expire', KEYS[1], ARGV[2])
            |end"""
                .trimMargin()
                .toByteArray()

        fun performAtomicSaveSetExpire(jedis: Jedis,
                                       key: ByteArray,
                                       value: ByteArray,
                                       timeToLive: ByteArray) {
            jedis.eval(LUA_ATOMIC_SAVE_SET_EXPIRE_SCRIPT,
                    1,
                    key,
                    value,
                    timeToLive)
        }

        fun openRedisConnection(redisConfig: RedisConfig): Jedis {
            val jedis = Jedis(redisConfig.host, redisConfig.port, redisConfig.timeout, redisConfig.useSsl)
            jedis.connect()
            if (redisConfig.password != null) {
                jedis.auth(redisConfig.password)
            }
            return jedis
        }
    }
}