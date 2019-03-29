package com.lykke.trade.volume.monitoring.service.config

import com.lykke.utils.logging.config.SlackNotificationConfig
import com.lykke.utils.logging.config.ThrottlingLoggerConfig

class Config(val tradeVolumeConfig: TradeVolumeConfig,
             val slackNotifications: SlackNotificationConfig,
             val throttlingLogger: ThrottlingLoggerConfig)