package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.trade.volume.monitoring.service.process.EventProcessLogger
import com.lykke.utils.logging.ThrottlingLogger

class EventProcessLoggerImpl(private val logger: ThrottlingLogger) : EventProcessLogger {

    override fun info(eventId: String, infoMessage: String) {
        logger.info(getFullMessage(eventId, infoMessage))
    }

    override fun debug(eventId: String, debugMessage: String) {
        logger.debug(getFullMessage(eventId, debugMessage))
    }

    override fun error(eventId: String, errorMessage: String) {
        logger.error(getFullMessage(eventId, errorMessage))
    }

    override fun error(eventId: String, errorMessage: String, t: Throwable) {
        logger.error(getFullMessage(eventId, errorMessage), t)
    }

    private fun getFullMessage(eventId: String, message: String) = "[$eventId] $message"
}