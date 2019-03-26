package com.lykke.trade.volume.monitoring.service.notification

import com.lykke.trade.volume.monitoring.service.notification.impl.MessageBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.lang.IllegalArgumentException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MessageBuilderTest {

    private lateinit var messageBuilder: MessageBuilder

    @Before
    fun init() {
        messageBuilder = MessageBuilder("%clientId_%tradeVolumeLimit_%targetAssetId_%assetId_%timestamp")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNotEnoughParamsProvided() {
        messageBuilder.setAssetId("BTC")
                .setClientId(UUID.randomUUID().toString())
                .build()
    }

    @Test
    fun testAllParametersProvided() {
        val clientId = UUID.randomUUID().toString()
        val assetId = "BTC"
        val targetAssetId = "USD"
        val tradeVolumeLimit = 100L
        val timestamp = Date()
        val timestampString = ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        val message = messageBuilder.setAssetId(assetId)
                .setClientId(clientId)
                .setTargetAssetId(targetAssetId)
                .setTradeVolumeLimit(tradeVolumeLimit)
                .setTimestamp(timestamp)
                .build()

        assertEquals("${clientId}_${tradeVolumeLimit}_${targetAssetId}_${assetId}_$timestampString", message)
    }
}