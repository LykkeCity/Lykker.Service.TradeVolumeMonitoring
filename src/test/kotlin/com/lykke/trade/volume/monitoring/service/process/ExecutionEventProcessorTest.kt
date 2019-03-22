package com.lykke.trade.volume.monitoring.service.process

import com.google.protobuf.Timestamp
import com.lykke.matching.engine.messages.outgoing.OutgoingMessages
import com.lykke.me.subscriber.incoming.events.proto.ProtoExecutionEvent
import com.lykke.trade.volume.monitoring.service.assertEquals
import com.lykke.trade.volume.monitoring.service.entity.TradeVolume
import com.lykke.trade.volume.monitoring.service.process.impl.ProtoExecutionEventProcessor
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.Date
import kotlin.test.assertEquals

class ExecutionEventProcessorTest {

    private lateinit var processor: ExecutionEventProcessor
    private val now = Date()

    @Before
    fun setUp() {
        processor = ProtoExecutionEventProcessor()
    }

    @Test
    fun testProcessEvent() {
        val event = buildEvent()
        val result = processor.process(event)

        assertEquals(1234, result.eventSequenceNumber)
        assertEquals(6, result.tradeVolumes.size)

        // trade 1
        assertTradeVolume(TradeVolume(0, "Wallet1",
                "Asset1", BigDecimal("1.01"), now), result.tradeVolumes[0])
        assertTradeVolume(TradeVolume(0, "Wallet1",
                "Asset2", BigDecimal("11.111"), now), result.tradeVolumes[1])

        // trade 2
        assertTradeVolume(TradeVolume(0, "Wallet1",
                "Asset1", BigDecimal("2"), now), result.tradeVolumes[2])
        assertTradeVolume(TradeVolume(0, "Wallet1",
                "Asset2", BigDecimal("3"), now), result.tradeVolumes[3])

        // trade 3
        assertTradeVolume(TradeVolume(0, "Wallet2",
                "Asset1", BigDecimal("100"), now), result.tradeVolumes[4])
        assertTradeVolume(TradeVolume(0, "Wallet2",
                "Asset3", BigDecimal("100.01"), now), result.tradeVolumes[5])
    }

    private fun assertTradeVolume(expected: TradeVolume, actual: TradeVolume) {
        assertEquals(expected.walletId, actual.walletId)
        assertEquals(expected.assetId, actual.assetId)
        assertEquals(expected.volume, actual.volume)
        assertEquals(expected.timestamp, actual.timestamp)
    }

    private fun buildEvent(): ProtoExecutionEvent {
        return ProtoExecutionEvent(buildProtoMessage())
    }

    private fun buildProtoMessage(): OutgoingMessages.ExecutionEvent {
        val builder = OutgoingMessages.ExecutionEvent.newBuilder()
        builder.header = OutgoingMessages.Header.newBuilder().setSequenceNumber(1234).build()
        builder.addOrders(buildProtoOrder("Wallet1", listOf(
                Trade("Asset1", "1.01", "Asset2", "11.111", now),
                Trade("Asset1", "2", "Asset2", "3", now)
        )))

        builder.addOrders(buildProtoOrder("Wallet2", listOf(
                Trade("Asset1", "100", "Asset3", "100.01", now)
        )))

        builder.addOrders(buildProtoOrder("Wallet2", emptyList()))

        return builder.build()
    }

    private fun buildProtoOrder(walletId: String, trades: Collection<Trade>): OutgoingMessages.ExecutionEvent.Order {
        val builder = OutgoingMessages.ExecutionEvent.Order.newBuilder()
        builder.walletId = walletId
        trades.forEach { trade ->
            builder.addTrades(
                    OutgoingMessages.ExecutionEvent.Order.Trade.newBuilder()
                            .setBaseAssetId(trade.baseAssetId)
                            .setBaseVolume(trade.baseAssetVolume)
                            .setQuotingAssetId(trade.quotingAssetId)
                            .setQuotingVolume(trade.quotingAssetVolume)
                            .setTimestamp(createProtobufTimestampBuilder(trade.timestamp))
            )
        }
        return builder.build()
    }

    private class Trade(val baseAssetId: String,
                        val baseAssetVolume: String,
                        val quotingAssetId: String,
                        val quotingAssetVolume: String,
                        val timestamp: Date)

    private fun createProtobufTimestampBuilder(date: Date): Timestamp.Builder {
        val instant = date.toInstant()
        return Timestamp.newBuilder()
                .setSeconds(instant.epochSecond)
                .setNanos(instant.nano)
    }
}