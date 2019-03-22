package com.lykke.trade.volume.monitoring.service.process

import com.lykke.me.subscriber.incoming.events.ExecutionEvent
import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper

interface ExecutionEventProcessor {
    fun process(event: ExecutionEvent): EventTradeVolumesWrapper
}