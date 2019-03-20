package com.lykke.trade.volume.monitoring.service.notification

import com.lykke.trade.volume.monitoring.service.config.Config
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class SentNotificationsCacheImpl(config: Config): SentNotificationsCache {
    private val notificationConfig = config.tradeVolumeConfig.notificationsConfig
    private val timestampByNotificationKey = ConcurrentHashMap<String, Date>()

    override fun add(clientId: String, assetId: String) {
        timestampByNotificationKey[getKey(clientId, assetId)] = Date()
    }

    override fun isSent(clientId: String, assetId: String) {
        timestampByNotificationKey[getKey(clientId, assetId)]
    }

    @Scheduled(fixedRateString = "#{Config.tradeVolumeConfig.notificationsConfig.throttlingPeriod}")
    private fun clean() {
        timestampByNotificationKey.forEach { key: String, timeStamp: Date ->
            if(Date().time - notificationConfig.throttlingPeriod >= timeStamp.time) {
                timestampByNotificationKey.remove(key)
            }
        }
    }

    private fun getKey(clientId: String, assetId: String): String {
        return "${clientId}_$assetId"
    }
}