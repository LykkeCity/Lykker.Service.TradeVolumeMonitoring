package com.lykke.trade.volume.monitoring.service.notification.impl

import com.lykke.trade.volume.monitoring.service.cache.SentNotificationsCache
import com.lykke.trade.volume.monitoring.service.config.NotificationsConfig
import com.lykke.trade.volume.monitoring.service.notification.MailNotificationService
import com.lykke.trade.volume.monitoring.service.notification.NotificationService
import com.lykke.utils.logging.MetricsLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.lang.Exception
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executor
import javax.annotation.PostConstruct

@Service
class NotificationServiceImpl(@Value("\${mail.message.format}") private val messageFormat: String,
                              @Value("\${mail.message.subject}") private val messageSubject: String,
                              private val mailNotificationService: MailNotificationService,
                              @Value("#{Config.tradeVolumeConfig.notificationsConfig}")
                              private val notificationsConfig: NotificationsConfig,
                              @Value("#{Config.tradeVolumeConfig.maxVolume}")
                              private val maxVolume: Long,
                              @Value("#{Config.tradeVolumeConfig.assetId}")
                              private val targetAssetId: String,
                              private val sentNotificationsCache: SentNotificationsCache,
                              private val applicationThreadPool: Executor,
                              val sendNotificationRequestQueue: BlockingQueue<SendNotificationRequest>) : NotificationService {

    private companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(NotificationServiceImpl::class.java)
        val METRICS_LOGGER = MetricsLogger.getLogger()
    }

    override fun sendTradeVolumeLimitReachedMailNotification(clientId: String, assetId: String, timestamp: Date) {
        sendNotificationRequestQueue.put(SendNotificationRequest(clientId, assetId, timestamp))
    }

    @PostConstruct
    private fun init() {
        applicationThreadPool.execute {
            while (true) {

                if (Thread.interrupted()) {
                    Thread.currentThread().interrupt()
                    return@execute
                }

                val request = sendNotificationRequestQueue.take()

                try {
                    if (sentNotificationsCache.isSent(request.clientId, request.assetId)) {
                        continue
                    }
                    val messageBuilder = MessageBodyBuilder(messageFormat)
                    val message = messageBuilder
                            .setClientId(request.clientId)
                            .setTradeVolumeLimit(maxVolume)
                            .setTargetAssetId(targetAssetId)
                            .setAssetId(request.assetId)
                            .setTimestamp(request.timestamp)
                            .build()

                    mailNotificationService.sendMail(notificationsConfig.mailAddresses, messageSubject, message)
                    sentNotificationsCache.add(clientId = request.clientId, assetId = request.assetId)
                    LOGGER.info(message)
                } catch (e: Exception) {
                    val message = "Error occurred when sending mail notification - volume limit reached for client ${request.clientId}, assetId: ${request.assetId}"
                    LOGGER.error(message, e)
                    METRICS_LOGGER.logError(message, e)
                }
            }
        }
    }

    class SendNotificationRequest(val clientId: String, val assetId: String, val timestamp: Date)
}