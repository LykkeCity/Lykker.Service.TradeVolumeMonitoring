package com.lykke.trade.volume.monitoring.service.process

import com.lykke.trade.volume.monitoring.service.process.impl.EventProcessLoggerImpl
import com.lykke.utils.logging.ThrottlingLogger

class EventProcessLoggerFactory {
    companion object {
        fun getLogger(name: String): EventProcessLogger {
            return EventProcessLoggerImpl(ThrottlingLogger.getLogger(name))
        }
    }
}