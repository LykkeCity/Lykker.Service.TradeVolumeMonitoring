package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.me.subscriber.incoming.events.ExecutionEvent
import com.lykke.me.subscriber.incoming.events.proto.MeProtoEvent
import com.lykke.trade.volume.monitoring.service.process.EventDeduplicationService
import com.lykke.trade.volume.monitoring.service.process.EventProcessLoggerFactory
import com.lykke.trade.volume.monitoring.service.process.MatchingEngineEventSubscriber
import com.lykke.utils.logging.MetricsLogger
import com.lykke.utils.notification.Listener
import java.util.concurrent.BlockingQueue

class MatchingEngineExecutionEventSubscriberImpl(private val eventDeduplicationService: EventDeduplicationService,
                                                 private val matchingEngineEventListener: Listener<MeProtoEvent<*>>,
                                                 private val outputQueue: BlockingQueue<ExecutionEvent>) : MatchingEngineEventSubscriber {

    companion object {
        private val LOGGER = EventProcessLoggerFactory.getLogger(MatchingEngineExecutionEventSubscriberImpl::class.java.name)
        private val METRICS_LOGGER = MetricsLogger.getLogger()
    }

    override fun notify(message: MeProtoEvent<*>) {
        try {
            handleIncomingEvent(message)
        } catch (e: Exception) {
            val logMessage = "Unable to read incoming event"
            LOGGER.error(message.sequenceNumber, logMessage, e)
            METRICS_LOGGER.logError(logMessage, e)
        }
    }

    private fun handleIncomingEvent(message: MeProtoEvent<*>) {
        LOGGER.debug(message.sequenceNumber, "Got incoming event, ME messageId: ${message.messageId}")
        if (!eventDeduplicationService.checkAndAdd(message.sequenceNumber)) {
            LOGGER.warn(message.sequenceNumber, "Duplicate")
            return
        }
        outputQueue.put(message as ExecutionEvent)
    }

    override fun subscribe() {
        matchingEngineEventListener.subscribe(this)
        LOGGER.info(null, "Subscribed")
    }
}