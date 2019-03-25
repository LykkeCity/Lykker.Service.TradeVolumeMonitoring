package com.lykke.trade.volume.monitoring.service.notification

import com.lykke.trade.volume.monitoring.service.getConfig
import com.lykke.trade.volume.monitoring.service.notification.impl.NotificationServiceImpl
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class NotificationServiceTest {

    @Mock
    private lateinit var mailNotificationService: MailNotificationService

    @Test
    fun sendNotification() {
        //given
        val targetAssetId = "USD"
        val maxVolume: Long = 100
        val clientId = "testClient"
        val assetId = "BTC"

        val messageSubject = "testSubject"
        val notificationsConfig = getConfig().tradeVolumeConfig.notificationsConfig
        val notificationService = NotificationServiceImpl("%clientId_%tradeVolumeLimit_%targetAssetId_%assetId",
                messageSubject,
                mailNotificationService,
                notificationsConfig,
                maxVolume,
                targetAssetId)

        //when
        notificationService.sendTradeVolumeLimitReachedMailNotification(clientId, assetId)

        //then
        notificationsConfig.mailAddress.forEach { mailAddress ->
            verify(mailNotificationService).sendMail(mailAddress, messageSubject, "${clientId}_${maxVolume}_${targetAssetId}_$assetId")
        }
    }
}