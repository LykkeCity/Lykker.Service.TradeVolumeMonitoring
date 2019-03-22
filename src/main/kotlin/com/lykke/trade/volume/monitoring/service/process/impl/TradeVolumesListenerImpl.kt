package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.process.EventProcessLoggerFactory
import com.lykke.trade.volume.monitoring.service.process.TradeVolumesListener
import com.lykke.trade.volume.monitoring.service.process.TradeVolumesProcessor
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executor

class TradeVolumesListenerImpl(private val id: Long,
                               private val executor: Executor,
                               private val inputQueue: BlockingQueue<EventTradeVolumesWrapper>,
                               private val processor: TradeVolumesProcessor) : TradeVolumesListener {

    companion object {
        private val LOGGER = EventProcessLoggerFactory.getLogger(TradeVolumesListenerImpl::class.java.name)
    }

    override fun startProcessingTradeVolumes() {
        executor.execute {
            while (true) {
                try {
                    processTradeVolumes(inputQueue.take())
                } catch (e: Exception) {
                    LOGGER.error(null, "Unable to take and process trade volumes: ${e.message}", e)
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
            LOGGER.error(tradeVolumes.eventSequenceNumber, "Unable to process trade volumes ($tradeVolumes): ${e.message}", e)
        }
    }
}