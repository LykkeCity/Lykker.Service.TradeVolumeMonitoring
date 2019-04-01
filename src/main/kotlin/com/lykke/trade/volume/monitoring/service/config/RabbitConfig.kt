package com.lykke.trade.volume.monitoring.service.config

class RabbitConfig(val uri: String,
                   val exchange: String,
                   val queueName: String,
                   val routingKey: String)