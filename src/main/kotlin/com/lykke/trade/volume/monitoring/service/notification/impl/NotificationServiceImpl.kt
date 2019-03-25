package com.lykke.trade.volume.monitoring.service.notification.impl

import com.lykke.trade.volume.monitoring.service.config.NotificationsConfig
import com.lykke.trade.volume.monitoring.service.notification.MailNotificationService
import com.lykke.trade.volume.monitoring.service.notification.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.lang.Exception

@Service
class NotificationServiceImpl(@Value("mail.message.format") private val messageFormat: String,
                              @Value("mail.message.subject") private val messageSubject: String,
                              private val mailNotificationService: MailNotificationService,
                              @Value("#{Config.tradeVolumeConfig.notificationsConfig}")
                              private val notificationsConfig: NotificationsConfig,
                              @Value("#{Config.tradeVolumeConfig.maxVolume}")
                              private val maxVolume: Long,
                              @Value("#{Config.tradeVolumeConfig.assetId}")
                              private val targetAssetId: String) : NotificationService {

    private companion object {
        val logger = LoggerFactory.getLogger(NotificationServiceImpl::class.java)
    }

    override fun sendTradeVolumeLimitReachedMailNotification(clientId: String, assetId: String) {
        val messageBuilder = MessageBuilder(messageFormat)
        val message = messageBuilder
                .setClientId(clientId)
                .setTradeVolumeLimit(maxVolume)
                .setTargetAssetId(targetAssetId)
                .setAssetId(assetId)
                .build()


        notificationsConfig.mailAddress.forEach { mailAddress ->
            try {
                mailNotificationService.sendMail(mailAddress, messageSubject, message)
            } catch (e: Exception) {
                logger.error("Error occurred when sending mail to address: $mailAddress volume limit reached for client $clientId, assetId: $assetId")
            }
        }

        logger.info(message)
    }
}