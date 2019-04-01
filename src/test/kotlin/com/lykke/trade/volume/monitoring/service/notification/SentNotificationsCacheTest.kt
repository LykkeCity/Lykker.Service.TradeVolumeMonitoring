package com.lykke.trade.volume.monitoring.service.notification

import com.lykke.trade.volume.monitoring.service.buildNotificationConfig
import com.lykke.trade.volume.monitoring.service.cache.SentNotificationsCache
import com.lykke.trade.volume.monitoring.service.cache.impl.SentNotificationsCacheImpl
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SentNotificationsCacheTest {

    private companion object {
        const val CLIENT1 = "CLIENT1"
        const val CLIENT2 = "CLIENT2"

        const val ASSET1 = "ASSET1"
        const val ASSET2 = "ASSET2"
        const val ASSET3 = "ASSET3"
    }

    lateinit var cache: SentNotificationsCache

    @Before
    fun init() {
        cache = SentNotificationsCacheImpl(buildNotificationConfig(), 1000L)
    }

    @Test
    fun testAdd() {
        cache.add(CLIENT1, ASSET1)
        cache.add(CLIENT2, ASSET2)

        assertTrue(cache.isSent(CLIENT1, ASSET1))
        assertTrue(cache.isSent(CLIENT2, ASSET2))
        assertFalse(cache.isSent(CLIENT2, ASSET3))
    }

    @Test
    fun testExpiredMessagesAreNotConsideredSent() {
        cache.add(CLIENT1, ASSET1)

        Thread.sleep(90)
        val cleanMethod = cache::class.java.getDeclaredMethod("clean")
        cleanMethod.isAccessible = true
        cleanMethod.invoke(cache)

        assertTrue(cache.isSent(CLIENT1, ASSET1))

        Thread.sleep(15)
        cache.add(CLIENT2, ASSET2)
        assertFalse(cache.isSent(CLIENT1, ASSET1))

        Thread.sleep(85)
        cleanMethod.invoke(cache)

        assertTrue(cache.isSent(CLIENT2, ASSET2))

        Thread.sleep(20)
        assertFalse(cache.isSent(CLIENT2, ASSET2))
    }

    @Test
    fun testClean() {
        cache.add(CLIENT1, ASSET1)
        cache.add(CLIENT2, ASSET2)

        //cache entries are not expired yet
        val cleanMethod = cache::class.java.getDeclaredMethod("clean")
        cleanMethod.isAccessible = true
        cleanMethod.invoke(cache)

        assertTrue(cache.isSent(CLIENT1, ASSET1))
        assertTrue(cache.isSent(CLIENT2, ASSET2))

        Thread.sleep(110)

        //cache entries should expire now
        cleanMethod.invoke(cache)
        assertFalse(cache.isSent(CLIENT1, ASSET1))
        assertFalse(cache.isSent(CLIENT2, ASSET2))

        cache.add(CLIENT1, ASSET1)
        cache.add(CLIENT1, ASSET3)

        assertTrue(cache.isSent(CLIENT1, ASSET1))
        assertTrue(cache.isSent(CLIENT1, ASSET3))
    }
}