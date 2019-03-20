package com.lykke.trade.volume.monitoring.service.notification

import com.lykke.trade.volume.monitoring.service.getConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SentNotificationsCacheTest {

    private companion object {
        val CLIENT1 = "CLIENT1"
        val CLIENT2 = "CLIENT2"

        val ASSET1 = "ASSET1"
        val ASSET2 = "ASSET2"
        val ASSET3 = "ASSET3"
    }

    lateinit var cache: SentNotificationsCache


    @Before
    fun init() {
        cache = SentNotificationsCacheImpl(getConfig())
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