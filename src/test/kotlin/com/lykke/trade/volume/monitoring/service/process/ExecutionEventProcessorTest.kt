package com.lykke.trade.volume.monitoring.service.process

import com.lykke.matching.engine.messages.outgoing.OutgoingMessages
import com.lykke.me.subscriber.incoming.events.ExecutionEvent
import com.lykke.me.subscriber.incoming.events.proto.ProtoExecutionEvent
import com.lykke.trade.volume.monitoring.service.assertEquals
import com.lykke.trade.volume.monitoring.service.entity.TradeVolume
import com.lykke.trade.volume.monitoring.service.process.impl.ProtoExecutionEventProcessor
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import kotlin.test.assertEquals

class ExecutionEventProcessorTest {

    private lateinit var processor: ExecutionEventProcessor

    @Before
    fun setUp() {
        processor = ProtoExecutionEventProcessor()
    }

    @Test
    fun testProcessEvent() {
        val result = processor.process(buildEvent())

        assertEquals("MessageId", result.messageId)
        assertEquals(6, result.tradeVolumes.size)

        assertTradeVolume(TradeVolume("Wallet1",
                "Asset1", BigDecimal("1.01")), result.tradeVolumes[0])
        assertTradeVolume(TradeVolume("Wallet1",
                "Asset2", BigDecimal("11.111")), result.tradeVolumes[1])
        assertTradeVolume(TradeVolume("Wallet1",
                "Asset1", BigDecimal("2")), result.tradeVolumes[2])
        assertTradeVolume(TradeVolume("Wallet1",
                "Asset2", BigDecimal("3")), result.tradeVolumes[3])
        assertTradeVolume(TradeVolume("Wallet2",
                "Asset1", BigDecimal("100")), result.tradeVolumes[4])
        assertTradeVolume(TradeVolume("Wallet2",
                "Asset3", BigDecimal("100.01")), result.tradeVolumes[5])
    }

    private fun assertTradeVolume(expected: TradeVolume, actual: TradeVolume) {
        assertEquals(expected.walletId, actual.walletId)
        assertEquals(expected.assetId, actual.assetId)
        assertEquals(expected.volume, actual.volume)
    }

    private fun buildEvent(): ExecutionEvent {
        return ProtoExecutionEvent(buildProtoMessage(), "MessageId")
    }

    private fun buildProtoMessage(): OutgoingMessages.ExecutionEvent {
        val builder = OutgoingMessages.ExecutionEvent.newBuilder()
        builder.addOrders(buildProtoOrder("Wallet1", listOf(
                Trade("Asset1", "1.01", "Asset2", "11.111"),
                Trade("Asset1", "2", "Asset2", "3")
        )))

        builder.addOrders(buildProtoOrder("Wallet2", listOf(
                Trade("Asset1", "100", "Asset3", "100.01")
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
            )
        }
        return builder.build()
    }

    private class Trade(val baseAssetId: String,
                        val baseAssetVolume: String,
                        val quotingAssetId: String,
                        val quotingAssetVolume: String)
}