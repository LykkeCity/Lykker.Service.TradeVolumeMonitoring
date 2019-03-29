package com.lykke.trade.volume.monitoring.service.config

data class RedisConfig(val host: String,
                       val port: Int,
                       val timeout: Int,
                       val useSsl: Boolean,
                       val password: String?,
                       val databaseIndex: Int)