package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.process.EventProcessLoggerFactory
import com.lykke.trade.volume.monitoring.service.process.TradeVolumesListener
import com.lykke.trade.volume.monitoring.service.process.TradeVolumesProcessor
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread

class TradeVolumesListenerImpl(private val id: Long,
                               private val inputQueue: BlockingQueue<EventTradeVolumesWrapper>,
                               private val processor: TradeVolumesProcessor) : TradeVolumesListener {

    companion object {
        private val LOGGER = EventProcessLoggerFactory.getLogger(TradeVolumesListenerImpl::class.java.name)
    }

    override fun startProcessingTradeVolumes() {
        thread(name = "TradeVolumesProcessingThread-$id") {
            while (true) {
                try {
                    processTradeVolumes(inputQueue.take())
                } catch (e: Exception) {
                    LOGGER.error("", "Unable to take and process trade volumes: ${e.message}", e)
                }
            }
        }
        LOGGER.info("", "Started, id: $id")
    }

    private fun processTradeVolumes(tradeVolumes: EventTradeVolumesWrapper) {
        try {
            processor.process(tradeVolumes)
            LOGGER.info(tradeVolumes.eventId, "Processed. Trade volumes: ${tradeVolumes.tradeVolumes.size}.")
        } catch (e: Exception) {
            LOGGER.error(tradeVolumes.eventId, "Unable to process trade volumes ($tradeVolumes): ${e.message}", e)
        }
    }
}