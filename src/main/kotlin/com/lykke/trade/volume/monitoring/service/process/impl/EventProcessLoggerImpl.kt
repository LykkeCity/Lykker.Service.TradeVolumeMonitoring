package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.trade.volume.monitoring.service.process.EventProcessLogger
import com.lykke.utils.logging.ThrottlingLogger

class EventProcessLoggerImpl(private val logger: ThrottlingLogger) : EventProcessLogger {

    override fun info(messageId: String, infoMessage: String) {
        logger.info(getFullMessage(messageId, infoMessage))
    }

    override fun debug(messageId: String, debugMessage: String) {
        logger.debug(getFullMessage(messageId, debugMessage))
    }

    override fun error(messageId: String, errorMessage: String) {
        logger.error(getFullMessage(messageId, errorMessage))
    }

    override fun error(messageId: String, errorMessage: String, t: Throwable) {
        logger.error(getFullMessage(messageId, errorMessage), t)
    }

    private fun getFullMessage(messageId: String, message: String) = "[$messageId] $message"
}