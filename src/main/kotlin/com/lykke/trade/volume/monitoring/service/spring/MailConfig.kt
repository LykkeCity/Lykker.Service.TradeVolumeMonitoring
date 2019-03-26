package com.lykke.trade.volume.monitoring.service.spring

import com.lykke.trade.volume.monitoring.service.config.Config
import com.lykke.trade.volume.monitoring.service.entity.MailApiType
import com.lykke.trade.volume.monitoring.service.notification.MailNotificationService
import com.lykke.trade.volume.monitoring.service.notification.MailValidator
import com.lykke.trade.volume.monitoring.service.notification.impl.AzureMailNotificationServiceImpl
import com.lykke.trade.volume.monitoring.service.notification.impl.HttpMailNotificationServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class MailConfig {

    @Autowired
    private lateinit var config: Config

    @Autowired
    private lateinit var mailValidator: MailValidator

    @Bean
    fun mailNotificationService(
            @Value("mail.notification.fomat")
            azureMessageFormat: String): MailNotificationService {
        val notificationsConfig = config.tradeVolumeConfig.notificationsConfig
        return when (notificationsConfig.type) {
            MailApiType.Azure -> AzureMailNotificationServiceImpl(notificationsConfig.azureConfig!!,
                    azureMessageFormat,
                    mailValidator,
                    notificationsConfig.senderAddress)
            MailApiType.PartnersRouterHttpApi -> HttpMailNotificationServiceImpl(notificationsConfig.httpConfig!!, mailValidator)
        }
    }
}