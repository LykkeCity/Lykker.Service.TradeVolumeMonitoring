package com.lykke.trade.volume.monitoring.service.isalive.impl

import com.lykke.trade.volume.monitoring.service.isalive.IsAliveResponse
import com.lykke.trade.volume.monitoring.service.isalive.IsAliveResponseGetter
import org.apache.http.HttpStatus

class IsAliveResponseGetterImpl(private val version: String) : IsAliveResponseGetter {
    override fun getResponse(): IsAliveResponse {
        return IsAliveResponse(HttpStatus.SC_OK, version)
    }
}