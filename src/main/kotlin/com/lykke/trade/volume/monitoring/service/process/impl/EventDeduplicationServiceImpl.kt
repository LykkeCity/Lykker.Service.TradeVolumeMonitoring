package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.trade.volume.monitoring.service.entity.ProcessedEvent
import com.lykke.trade.volume.monitoring.service.process.EventDeduplicationService

// todo: additional task: implement deduplication
class EventDeduplicationServiceImpl : EventDeduplicationService {
    override fun isDuplicate(sequenceNumber: Long): Boolean {
        return false
    }

    override fun addProcessedEvent(processedEvent: ProcessedEvent) {

    }

}