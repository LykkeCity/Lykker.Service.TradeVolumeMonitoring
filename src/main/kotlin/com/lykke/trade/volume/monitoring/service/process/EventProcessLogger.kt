package com.lykke.trade.volume.monitoring.service.process

interface EventProcessLogger {
    fun info(eventSequenceNumber: Long?, infoMessage: String)
    fun debug(eventSequenceNumber: Long?, debugMessage: String)
    fun error(eventSequenceNumber: Long?, errorMessage: String)
    fun error(eventSequenceNumber: Long?, errorMessage: String, t: Throwable)
}