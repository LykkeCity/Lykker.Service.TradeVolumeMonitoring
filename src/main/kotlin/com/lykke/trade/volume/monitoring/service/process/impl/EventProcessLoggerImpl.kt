package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.trade.volume.monitoring.service.process.EventProcessLogger
import com.lykke.utils.logging.ThrottlingLogger

class EventProcessLoggerImpl(private val logger: ThrottlingLogger) : EventProcessLogger {

    override fun debug(eventSequenceNumber: Long?, debugMessage: String) {
        logger.debug(getFullMessage(eventSequenceNumber, debugMessage))
    }

    override fun info(eventSequenceNumber: Long?, infoMessage: String) {
        logger.info(getFullMessage(eventSequenceNumber, infoMessage))
    }

    override fun warn(eventSequenceNumber: Long?, debugMessage: String) {
        logger.warn(getFullMessage(eventSequenceNumber, debugMessage))
    }

    override fun error(eventSequenceNumber: Long?, errorMessage: String) {
        logger.error(getFullMessage(eventSequenceNumber, errorMessage))
    }

    override fun error(eventSequenceNumber: Long?, errorMessage: String, t: Throwable) {
        logger.error(getFullMessage(eventSequenceNumber, errorMessage), t)
    }

    private fun getFullMessage(eventSequenceNumber: Long?, message: String) = eventSequenceNumber?.let { "[$it] $message" }
            ?: message
}