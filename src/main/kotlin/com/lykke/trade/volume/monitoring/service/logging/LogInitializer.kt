package com.lykke.trade.volume.monitoring.service.logging

import com.lykke.utils.logging.MetricsLogger
import com.lykke.utils.logging.ThrottlingLogger
import com.lykke.utils.logging.config.SlackNotificationConfig
import com.lykke.utils.logging.config.ThrottlingLoggerConfig

class LogInitializer(private val slackNotificationConfig: SlackNotificationConfig,
                     private val throttlingLoggerConfig: ThrottlingLoggerConfig) {

    companion object {
        private const val SLACK_NOTIFICATION_SENDER_NAME = "ME.TradeVolumeMonitoring"
    }

    fun init() {
        MetricsLogger.init(SLACK_NOTIFICATION_SENDER_NAME, slackNotificationConfig)
        ThrottlingLogger.init(throttlingLoggerConfig)
    }
}