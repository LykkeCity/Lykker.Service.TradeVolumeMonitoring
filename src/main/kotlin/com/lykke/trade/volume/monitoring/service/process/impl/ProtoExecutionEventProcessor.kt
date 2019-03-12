package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.me.subscriber.incoming.events.ExecutionEvent
import com.lykke.me.subscriber.incoming.events.proto.ProtoExecutionEvent
import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.entity.TradeVolume
import com.lykke.trade.volume.monitoring.service.process.ExecutionEventProcessor

class ProtoExecutionEventProcessor : ExecutionEventProcessor {

    override fun process(event: ExecutionEvent): EventTradeVolumesWrapper {
        event as ProtoExecutionEvent
        return EventTradeVolumesWrapper(event.messageId, event.message.ordersList
                .flatMap { order ->
            order.tradesList.flatMap { orderTrade ->
                val baseAssetOperation = TradeVolume(order.walletId,
                        orderTrade.baseAssetId,
                        orderTrade.baseVolume.toBigDecimal().abs())
                val quotingAssetOperation = TradeVolume(order.walletId,
                        orderTrade.quotingAssetId,
                        orderTrade.quotingVolume.toBigDecimal().abs())
                listOf(baseAssetOperation, quotingAssetOperation)
            }
        })
    }
}