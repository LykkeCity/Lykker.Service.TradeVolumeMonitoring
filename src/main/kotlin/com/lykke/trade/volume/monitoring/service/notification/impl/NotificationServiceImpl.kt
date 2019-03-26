package com.lykke.trade.volume.monitoring.service.notification.impl

import com.lykke.trade.volume.monitoring.service.cache.SentNotificationsCache
import com.lykke.trade.volume.monitoring.service.config.NotificationsConfig
import com.lykke.trade.volume.monitoring.service.notification.MailNotificationService
import com.lykke.trade.volume.monitoring.service.notification.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.lang.Exception
import java.util.*

@Service
class NotificationServiceImpl(@Value("mail.message.format") private val messageFormat: String,
                              @Value("mail.message.subject") private val messageSubject: String,
                              private val mailNotificationService: MailNotificationService,
                              @Value("#{Config.tradeVolumeConfig.notificationsConfig}")
                              private val notificationsConfig: NotificationsConfig,
                              @Value("#{Config.tradeVolumeConfig.maxVolume}")
                              private val maxVolume: Long,
                              @Value("#{Config.tradeVolumeConfig.assetId}")
                              private val targetAssetId: String,
                              private val sentNotificationsCache: SentNotificationsCache) : NotificationService {

    private companion object {
        val logger = LoggerFactory.getLogger(NotificationServiceImpl::class.java)
    }

    override fun sendTradeVolumeLimitReachedMailNotification(clientId: String, assetId: String, timestamp: Date) {
        if (sentNotificationsCache.isSent(clientId, assetId)) {
            return
        }
        val messageBuilder = MessageBuilder(messageFormat)
        val message = messageBuilder
                .setClientId(clientId)
                .setTradeVolumeLimit(maxVolume)
                .setTargetAssetId(targetAssetId)
                .setAssetId(assetId)
                .setTimestamp(timestamp)
                .build()

        try {
            mailNotificationService.sendMail(notificationsConfig.mailAddresses, messageSubject, message)
        } catch (e: Exception) {
            logger.error("Error occurred when sending mail notification - volume limit reached for client $clientId, assetId: $assetId")
        }
        sentNotificationsCache.add(clientId = clientId, assetId =  assetId)

        logger.info(message)
    }
}