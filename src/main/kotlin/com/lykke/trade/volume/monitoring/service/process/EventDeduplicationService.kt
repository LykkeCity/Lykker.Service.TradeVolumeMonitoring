package com.lykke.trade.volume.monitoring.service.process

import com.lykke.trade.volume.monitoring.service.entity.ProcessedEvent

interface EventDeduplicationService {
    fun isDuplicate(eventId: String): Boolean
    fun addProcessedEvent(processedEvent: ProcessedEvent)
}