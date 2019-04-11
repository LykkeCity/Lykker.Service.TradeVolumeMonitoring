package com.lykke.trade.volume.monitoring.service.process.impl

import com.google.protobuf.Timestamp
import com.lykke.client.accounts.ClientAccountsCache
import com.lykke.matching.engine.messages.outgoing.OutgoingMessages
import com.lykke.me.subscriber.incoming.events.ExecutionEvent
import com.lykke.me.subscriber.incoming.events.proto.ProtoExecutionEvent
import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.entity.TradeVolume
import com.lykke.trade.volume.monitoring.service.process.EventProcessLoggerFactory
import com.lykke.trade.volume.monitoring.service.process.ExecutionEventProcessor
import java.time.Instant
import java.util.Date

class ProtoExecutionEventProcessor(private val clientAccountsCache: ClientAccountsCache,
                                   private val ignoredClientIds: Set<String>) : ExecutionEventProcessor {

    companion object {
        private val LOGGER = EventProcessLoggerFactory.getLogger(ProtoExecutionEventProcessor::class.java.name)
    }

    override fun process(event: ExecutionEvent): EventTradeVolumesWrapper {
        event as ProtoExecutionEvent

        return EventTradeVolumesWrapper(event.sequenceNumber,
                convertToDate(event.message.header.timestamp),
                event.message.ordersList
                        .flatMap { order ->
                            order.tradesList.flatMap { orderTrade ->
                                convertOrderTradeToClientTradeVolumes(event.sequenceNumber,
                                        order.walletId,
                                        orderTrade)
                            }
                        })
    }

    private fun convertOrderTradeToClientTradeVolumes(sequenceNumber: Long,
                                                      orderWalletId: String,
                                                      orderTrade: OutgoingMessages.ExecutionEvent.Order.Trade): List<TradeVolume> {
        val clientId = clientAccountsCache.getClientByWalletId(orderWalletId)
        if (clientId == null) {
            LOGGER.error(sequenceNumber, "Can not find client by wallet: $orderWalletId")
            return emptyList()
        }
        if (ignoredClientIds.contains(clientId)) {
            LOGGER.debug(sequenceNumber, "Ignored trades, clientId: $clientId, walletId: $orderWalletId")
            return emptyList()
        }
        val baseAssetOperation = TradeVolume(orderTrade.index,
                orderWalletId,
                clientId,
                orderTrade.baseAssetId,
                orderTrade.baseVolume.toBigDecimal().abs(),
                convertToDate(orderTrade.timestamp))
        val quotingAssetOperation = TradeVolume(orderTrade.index,
                orderWalletId,
                clientId,
                orderTrade.quotingAssetId,
                orderTrade.quotingVolume.toBigDecimal().abs(),
                convertToDate(orderTrade.timestamp))
        return listOf(baseAssetOperation, quotingAssetOperation)
    }

    private fun convertToDate(protoTimestamp: Timestamp): Date {
        return Date.from(Instant.ofEpochSecond(protoTimestamp.seconds,
                protoTimestamp.nanos.toLong()))
    }
}