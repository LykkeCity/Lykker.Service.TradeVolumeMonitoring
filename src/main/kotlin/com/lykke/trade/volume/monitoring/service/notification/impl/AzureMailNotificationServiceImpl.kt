package com.lykke.trade.volume.monitoring.service.notification.impl

import com.lykke.trade.volume.monitoring.service.config.AzureNotificationConfig
import com.lykke.trade.volume.monitoring.service.notification.MailNotificationService
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.queue.CloudQueue
import com.microsoft.azure.storage.queue.CloudQueueMessage
import org.apache.commons.lang3.StringUtils
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class AzureMailNotificationServiceImpl(
        @Value("#{Config.tradeVolumeConfig.notificationsConfig.azureConfig}")
        azureNotificationConfig: AzureNotificationConfig) : MailNotificationService {
    private val mailQueue: CloudQueue = CloudStorageAccount
            .parse(azureNotificationConfig.connectionString)
            .createCloudQueueClient()
            .getQueueReference(azureNotificationConfig.queueName)

    init {
        if (!mailQueue.exists()) {
            throw IllegalArgumentException("Emails queue does not exists")
        }
    }

    override fun sendMail(email: String, subject: String, textBody: String) {
        validate(email, subject, textBody)

        mailQueue.addMessage(CloudQueueMessage(""))
    }

    private fun validate(email: String, subject: String, textBody: String) {
        if (!EmailValidator.getInstance().isValid(email)) {
            throw IllegalArgumentException("Mail mail address is invalid, mail can not be sent, subject $subject")
        }

        if (StringUtils.isEmpty(subject)) {
            throw IllegalArgumentException("Mail subject can not be empty")
        }

        if (StringUtils.isEmpty(textBody)) {
            throw IllegalArgumentException("Body of the mail can not be empty")
        }
    }
}