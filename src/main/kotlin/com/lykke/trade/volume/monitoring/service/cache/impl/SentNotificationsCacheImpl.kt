package com.lykke.trade.volume.monitoring.service.cache.impl

import com.lykke.trade.volume.monitoring.service.config.NotificationsConfig
import com.lykke.trade.volume.monitoring.service.cache.SentNotificationsCache
import com.lykke.trade.volume.monitoring.service.entity.SentNotificationRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class SentNotificationsCacheImpl(@Value("#{Config.tradeVolumeConfig.notificationsConfig}")
                                 val notificationConfig: NotificationsConfig,
                                 @Value("#{Config.tradeVolumeConfig.tradeVolumeCacheConfig.volumePeriod}")
                                 val volumePeriod: Long): SentNotificationsCache {
    private val sentNotificationByKey = ConcurrentHashMap<String, SentNotificationRecord>()

    override fun add(clientId: String, assetId: String) {
        sentNotificationByKey[getKey(clientId, assetId)] = SentNotificationRecord(clientId = clientId,
                assetId = assetId, timestamp =  Date())
    }

    override fun isSent(clientId: String, assetId: String): Boolean {
        val notification = sentNotificationByKey[getKey(clientId, assetId)]
        return notification != null && notification.timestamp.time > Date().time - notificationConfig.throttlingPeriod
    }

    override fun getAllNotificationsForLastTradePeriod(): List<SentNotificationRecord> {
        return LinkedList(sentNotificationByKey.values)
    }

    @Scheduled(fixedRateString = "#{Config.tradeVolumeConfig.notificationsConfig.throttlingPeriod}")
    private fun clean() {
        sentNotificationByKey.forEach { key: String, notification: SentNotificationRecord ->
            if(Date().time - volumePeriod >= notification.timestamp.time) {
                sentNotificationByKey.remove(key)
            }
        }
    }

    private fun getKey(clientId: String, assetId: String): String {
        return "${clientId}_$assetId"
    }
}