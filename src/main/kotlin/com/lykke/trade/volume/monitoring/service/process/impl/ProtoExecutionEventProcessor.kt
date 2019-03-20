package com.lykke.trade.volume.monitoring.service.process.impl

import com.google.protobuf.Timestamp
import com.lykke.me.subscriber.incoming.events.ExecutionEvent
import com.lykke.me.subscriber.incoming.events.proto.ProtoExecutionEvent
import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.entity.TradeVolume
import com.lykke.trade.volume.monitoring.service.process.ExecutionEventProcessor
import java.time.Instant
import java.util.Date

class ProtoExecutionEventProcessor : ExecutionEventProcessor {

    override fun process(event: ExecutionEvent): EventTradeVolumesWrapper {
        event as ProtoExecutionEvent

        return EventTradeVolumesWrapper(event.sequenceNumber,
                event.message.ordersList
                        .flatMap { order ->
                            order.tradesList.flatMap { orderTrade ->
                                val baseAssetOperation = TradeVolume(orderTrade.index,
                                        order.walletId,
                                        orderTrade.baseAssetId,
                                        orderTrade.baseVolume.toBigDecimal().abs(),
                                        convertToDate(orderTrade.timestamp))
                                val quotingAssetOperation = TradeVolume(orderTrade.index,
                                        order.walletId,
                                        orderTrade.quotingAssetId,
                                        orderTrade.quotingVolume.toBigDecimal().abs(),
                                        convertToDate(orderTrade.timestamp))
                                listOf(baseAssetOperation, quotingAssetOperation)
                            }
                        })
    }

    private fun convertToDate(protoTimestamp: Timestamp): Date {
        return Date.from(Instant.ofEpochSecond(protoTimestamp.seconds,
                protoTimestamp.nanos.toLong()))
    }
}