package com.lykke.trade.volume.monitoring.service.config

import com.lykke.trade.volume.monitoring.service.entity.MailApiType

class NotificationsConfig(
        val type: MailApiType,
        val azureConfig: AzureNotificationConfig?,
        val httpConfig: HttpApiConfig?,
        val throttlingPeriod: Long,
        val senderAddress: String,
        val mailAddress: List<String>)