package com.lykke.trade.volume.monitoring.service.notification

import com.lykke.trade.volume.monitoring.service.cache.SentNotificationsCache
import com.lykke.trade.volume.monitoring.service.getConfig
import com.lykke.trade.volume.monitoring.service.notification.impl.NotificationServiceImpl
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.junit.MockitoJUnitRunner
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

@RunWith(MockitoJUnitRunner::class)
class NotificationServiceTest {

    @Mock
    private lateinit var mailNotificationService: MailNotificationService

    @Mock
    private lateinit var sentNotificationsCache: SentNotificationsCache

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
                targetAssetId,
                sentNotificationsCache,
                Executors.newCachedThreadPool(),
                LinkedBlockingQueue())

        //when
        notificationService.sendTradeVolumeLimitReachedMailNotification(clientId, assetId, Date())

        //then
        verify(mailNotificationService).sendMail(notificationsConfig.mailAddresses, messageSubject, "${clientId}_${maxVolume}_${targetAssetId}_$assetId")
        verify(sentNotificationsCache).add(clientId, assetId)
    }

    @Test
    fun testNotificationNotSentIfItWasAlreadySent() {
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
                targetAssetId,
                sentNotificationsCache)
        whenever(sentNotificationsCache.isSent(eq(clientId), eq(assetId))).thenReturn(true)

        //when
        notificationService.sendTradeVolumeLimitReachedMailNotification(clientId, assetId, Date())

        //then
        verify(mailNotificationService, never()).sendMail(notificationsConfig.mailAddresses, messageSubject, "${clientId}_${maxVolume}_${targetAssetId}_$assetId")
    }
}