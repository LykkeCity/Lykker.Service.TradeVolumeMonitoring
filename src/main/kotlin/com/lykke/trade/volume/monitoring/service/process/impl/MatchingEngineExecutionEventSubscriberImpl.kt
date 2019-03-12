package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.me.subscriber.incoming.events.ExecutionEvent
import com.lykke.me.subscriber.incoming.events.proto.MeProtoEvent
import com.lykke.trade.volume.monitoring.service.process.EventProcessLoggerFactory
import com.lykke.trade.volume.monitoring.service.process.MatchingEngineEventSubscriber
import com.lykke.utils.notification.Listener
import java.util.concurrent.BlockingQueue

class MatchingEngineExecutionEventSubscriberImpl(private val matchingEngineEventListener: Listener<MeProtoEvent<*>>,
                                                 private val outputQueue: BlockingQueue<ExecutionEvent>) : MatchingEngineEventSubscriber {

    companion object {
        private val LOGGER = EventProcessLoggerFactory.getLogger(MatchingEngineExecutionEventSubscriberImpl::class.java.name)
    }

    override fun notify(message: MeProtoEvent<*>) {
        try {
            LOGGER.debug(message.messageId, "Got incoming event")
            outputQueue.put(message as ExecutionEvent)
        } catch (e: Exception) {
            LOGGER.error(message.messageId,
                    "Unable to read incoming event: ${e.message}",
                    e)
        }
    }

    override fun subscribe() {
        matchingEngineEventListener.subscribe(this)
        LOGGER.info("", "Subscribed")
    }
}