package com.lykke.trade.volume.monitoring.service.config

class NotificationsConfig(
        val azureConfig: AzureNotificationConfig,
        val throttlingPeriod: Long,
        val mailAddress: List<String>)