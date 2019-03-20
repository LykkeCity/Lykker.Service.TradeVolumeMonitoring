package com.lykke.trade.volume.monitoring.service.process

interface EventProcessLogger {
    fun info(eventId: String, infoMessage: String)
    fun debug(eventId: String, debugMessage: String)
    fun error(eventId: String, errorMessage: String)
    fun error(eventId: String, errorMessage: String, t: Throwable)
}