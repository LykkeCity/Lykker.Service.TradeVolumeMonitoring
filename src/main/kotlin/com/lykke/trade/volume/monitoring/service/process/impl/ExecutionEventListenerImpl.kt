package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.me.subscriber.incoming.events.ExecutionEvent
import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.process.EventProcessLoggerFactory
import com.lykke.trade.volume.monitoring.service.process.ExecutionEventListener
import com.lykke.trade.volume.monitoring.service.process.ExecutionEventProcessor
import com.lykke.utils.logging.MetricsLogger
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executor

class ExecutionEventListenerImpl(private val id: Long,
                                 private val executor: Executor,
                                 private val inputQueue: BlockingQueue<ExecutionEvent>,
                                 private val processor: ExecutionEventProcessor,
                                 private val outputQueue: BlockingQueue<EventTradeVolumesWrapper>) : ExecutionEventListener {

    companion object {
        private val LOGGER = EventProcessLoggerFactory.getLogger(ExecutionEventListenerImpl::class.java.name)
        private val METRICS_LOGGER = MetricsLogger.getLogger()
    }

    override fun startProcessingExecutionEvents() {
        executor.execute {
            while (true) {
                try {
                    processEvent(inputQueue.take())
                } catch (e: Exception) {
                    val message = "Unable to take and process event"
                    LOGGER.error(null, message, e)
                    METRICS_LOGGER.logError(message, e)
                }
            }
        }
        LOGGER.info(null, "Started, id: $id")
    }

    private fun processEvent(event: ExecutionEvent) {
        try {
            val tradeVolumes = processor.process(event)
            if (tradeVolumes.tradeVolumes.isEmpty()) {
                LOGGER.debug(event.sequenceNumber, "No trades in event")
                return
            }
            outputQueue.put(tradeVolumes)
        } catch (e: Exception) {
            val message = "Unable to process incoming execution event"
            LOGGER.error(event.sequenceNumber, message, e)
            METRICS_LOGGER.logError(message, e)
        }
    }
}