package com.lykke.trade.volume.monitoring.service.notification

import com.lykke.trade.volume.monitoring.service.buildNotificationConfig
import com.lykke.trade.volume.monitoring.service.cache.SentNotificationsCache
import com.lykke.trade.volume.monitoring.service.notification.impl.NotificationServiceImpl
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.util.ReflectionUtils
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

@RunWith(MockitoJUnitRunner::class)
class NotificationServiceTest {

    @Mock
    private lateinit var mailNotificationService: MailNotificationService

    @Mock
    private lateinit var sentNotificationsCache: SentNotificationsCache

    private lateinit var notificationService: NotificationService

    private companion object {
        const val targetAssetId = "USD"
        const val maxVolume: Long = 100
        const val clientId = "testClient"
        const val assetId = "BTC"
        const val messageSubject = "testSubject"
        val notificationsConfig = buildNotificationConfig()
    }

    @Before
    fun init() {
        notificationService = NotificationServiceImpl("%clientId_%tradeVolumeLimit_%targetAssetId_%assetId",
                messageSubject,
                mailNotificationService,
                notificationsConfig,
                maxVolume,
                targetAssetId,
                sentNotificationsCache,
                Executors.newCachedThreadPool(),
                LinkedBlockingQueue())

        val initMethod = ReflectionUtils.findMethod(NotificationServiceImpl::class.java, "init")
        initMethod!!.isAccessible = true
        initMethod.invoke(notificationService)
    }

    @Test
    fun sendNotification() {
        //when
        notificationService.sendTradeVolumeLimitReachedMailNotification(clientId, assetId, Date())
        Thread.sleep(200)

        //then
        verify(mailNotificationService).sendMail(notificationsConfig.mailAddresses, messageSubject, "${clientId}_${maxVolume}_${targetAssetId}_$assetId")
        verify(sentNotificationsCache).add(clientId, assetId)
    }

    @Test
    fun testNotificationNotSentIfItWasAlreadySent() {
        //given
        whenever(sentNotificationsCache.isSent(eq(clientId), eq(assetId))).thenReturn(true)

        //when
        notificationService.sendTradeVolumeLimitReachedMailNotification(clientId, assetId, Date())
        Thread.sleep(200)

        //then
        verify(mailNotificationService, never()).sendMail(notificationsConfig.mailAddresses, messageSubject, "${clientId}_${maxVolume}_${targetAssetId}_$assetId")
    }
}