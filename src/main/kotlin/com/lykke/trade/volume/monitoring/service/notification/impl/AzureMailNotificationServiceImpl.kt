package com.lykke.trade.volume.monitoring.service.notification.impl

import com.lykke.trade.volume.monitoring.service.config.AzureNotificationConfig
import com.lykke.trade.volume.monitoring.service.notification.MailNotificationService
import com.lykke.trade.volume.monitoring.service.notification.MailValidator
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.queue.CloudQueue
import com.microsoft.azure.storage.queue.CloudQueueMessage
import java.lang.IllegalArgumentException

class AzureMailNotificationServiceImpl(
        azureNotificationConfig: AzureNotificationConfig,
        private val messageFormat: String,
        private val validator: MailValidator,
        private val senderAddress: String) : MailNotificationService {
    private val mailQueue: CloudQueue = CloudStorageAccount
            .parse(azureNotificationConfig.connectionString)
            .createCloudQueueClient()
            .getQueueReference(azureNotificationConfig.queueName)

    init {
        if (!mailQueue.exists()) {
            throw IllegalArgumentException("Emails queue does not exists")
        }
    }

    override fun sendMail(emailAddresses: List<String>, subject: String, textBody: String) {
        validator.validate(emailAddresses, subject, textBody)

        val sendMailRequestBuilder = SendMailRequestBuilder(messageFormat)
        sendMailRequestBuilder
                .setBody(textBody)
                .setSubject(subject)
                .setSenderAddress(senderAddress)

        emailAddresses.forEach {
            sendMailRequestBuilder.setMailAddress(it)
            mailQueue.addMessage(CloudQueueMessage(sendMailRequestBuilder.build()))
        }
    }
}