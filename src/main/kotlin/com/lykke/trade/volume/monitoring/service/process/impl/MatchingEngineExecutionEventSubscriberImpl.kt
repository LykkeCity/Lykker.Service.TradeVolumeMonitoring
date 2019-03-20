package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.me.subscriber.incoming.events.ExecutionEvent
import com.lykke.me.subscriber.incoming.events.proto.MeProtoEvent
import com.lykke.trade.volume.monitoring.service.entity.ProcessedEvent
import com.lykke.trade.volume.monitoring.service.process.EventDeduplicationService
import com.lykke.trade.volume.monitoring.service.process.EventProcessLoggerFactory
import com.lykke.trade.volume.monitoring.service.process.MatchingEngineEventSubscriber
import com.lykke.utils.notification.Listener
import java.util.Date
import java.util.concurrent.BlockingQueue

class MatchingEngineExecutionEventSubscriberImpl(private val eventDeduplicationService: EventDeduplicationService,
                                                 private val matchingEngineEventListener: Listener<MeProtoEvent<*>>,
                                                 private val outputQueue: BlockingQueue<ExecutionEvent>) : MatchingEngineEventSubscriber {

    companion object {
        private val LOGGER = EventProcessLoggerFactory.getLogger(MatchingEngineExecutionEventSubscriberImpl::class.java.name)
    }

    override fun notify(message: MeProtoEvent<*>) {
        try {
            handleIncomingEvent(message)
        } catch (e: Exception) {
            LOGGER.error(message.sequenceNumber,
                    "Unable to read incoming event: ${e.message}",
                    e)
        }
    }

    private fun handleIncomingEvent(message: MeProtoEvent<*>) {
        LOGGER.debug(message.sequenceNumber, "Got incoming event, ME messageId: ${message.messageId}")
        if (eventDeduplicationService.isDuplicate(message.sequenceNumber)) {
            LOGGER.error(message.sequenceNumber, "Duplicate")
            return
        }
        eventDeduplicationService.addProcessedEvent(ProcessedEvent(message.sequenceNumber, Date().time))
        outputQueue.put(message as ExecutionEvent)
    }

    override fun subscribe() {
        matchingEngineEventListener.subscribe(this)
        LOGGER.info(null, "Subscribed")
    }
}