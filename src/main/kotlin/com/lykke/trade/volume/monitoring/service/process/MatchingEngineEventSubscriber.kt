package com.lykke.trade.volume.monitoring.service.process

import com.lykke.me.subscriber.incoming.events.proto.MeProtoEvent
import com.lykke.utils.notification.Subscriber

interface MatchingEngineEventSubscriber : Subscriber<MeProtoEvent<*>> {
    fun subscribe()
}