package com.lykke.trade.volume.monitoring.service.exception

class ApplicationException(message: String) : Exception(message) {
    override val message: String
        get() = super.message!!
}