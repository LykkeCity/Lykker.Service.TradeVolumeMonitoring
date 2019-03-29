package com.lykke.trade.volume.monitoring.service.process

interface EventDeduplicationService {
    fun checkAndAdd(sequenceNumber: Long): Boolean
}