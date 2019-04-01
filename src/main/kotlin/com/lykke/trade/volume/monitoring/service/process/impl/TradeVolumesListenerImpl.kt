package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.process.EventProcessLoggerFactory
import com.lykke.trade.volume.monitoring.service.process.TradeVolumesListener
import com.lykke.trade.volume.monitoring.service.process.TradeVolumesProcessor
import com.lykke.utils.logging.MetricsLogger
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executor

class TradeVolumesListenerImpl(private val id: Long,
                               private val executor: Executor,
                               private val inputQueue: BlockingQueue<EventTradeVolumesWrapper>,
                               private val processor: TradeVolumesProcessor) : TradeVolumesListener {

    companion object {
        private val LOGGER = EventProcessLoggerFactory.getLogger(TradeVolumesListenerImpl::class.java.name)
        private val METRICS_LOGGER = MetricsLogger.getLogger()
    }

    override fun startProcessingTradeVolumes() {
        executor.execute {
            while (true) {
                try {
                    processTradeVolumes(inputQueue.take())
                } catch (e: Exception) {
                    val message = "Unable to take and process trade volumes"
                    LOGGER.error(null, message, e)
                    METRICS_LOGGER.logError(message, e)
                }
            }
        }
        LOGGER.info(null, "Started, id: $id")
    }

    private fun processTradeVolumes(tradeVolumes: EventTradeVolumesWrapper) {
        try {
            processor.process(tradeVolumes)
            LOGGER.info(tradeVolumes.eventSequenceNumber, "Processed. Trade volumes: ${tradeVolumes.tradeVolumes.size}.")
        } catch (e: Exception) {
            val message = "Unable to process trade volumes ($tradeVolumes)"
            LOGGER.error(tradeVolumes.eventSequenceNumber, message, e)
            METRICS_LOGGER.logError(message, e)
        }
    }
}