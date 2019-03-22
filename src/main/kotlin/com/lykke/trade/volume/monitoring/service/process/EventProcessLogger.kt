package com.lykke.trade.volume.monitoring.service.process

interface EventProcessLogger {
    fun debug(eventSequenceNumber: Long?, debugMessage: String)
    fun info(eventSequenceNumber: Long?, infoMessage: String)
    fun warn(eventSequenceNumber: Long?, debugMessage: String)
    fun error(eventSequenceNumber: Long?, errorMessage: String)
    fun error(eventSequenceNumber: Long?, errorMessage: String, t: Throwable)
}