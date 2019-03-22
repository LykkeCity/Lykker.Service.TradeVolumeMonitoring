package com.lykke.trade.volume.monitoring.service.process

import com.lykke.trade.volume.monitoring.service.entity.ProcessedEvent

interface EventDeduplicationService {
    fun isDuplicate(sequenceNumber: Long): Boolean
    fun addProcessedEvent(processedEvent: ProcessedEvent)
}