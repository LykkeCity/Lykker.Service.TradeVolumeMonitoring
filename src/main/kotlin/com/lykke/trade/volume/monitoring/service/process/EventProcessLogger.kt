package com.lykke.trade.volume.monitoring.service.process

interface EventProcessLogger {
    fun info(messageId: String, infoMessage: String)
    fun debug(messageId: String, debugMessage: String)
    fun error(messageId: String, errorMessage: String)
    fun error(messageId: String, errorMessage: String, t: Throwable)
}