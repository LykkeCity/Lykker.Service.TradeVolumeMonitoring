package com.lykke.trade.volume.monitoring.service.notification.impl

import com.lykke.trade.volume.monitoring.service.notification.MailValidator
import org.apache.commons.lang3.StringUtils
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException

@Component
class MailValidatorImpl : MailValidator {
    override fun validate(emailAddresses: List<String>, subject: String, textBody: String) {
        emailAddresses.forEach { mail ->
            if (!EmailValidator.getInstance().isValid(mail)) {
                throw IllegalArgumentException("Mail mail address is invalid, mail can not be sent, subject $subject")
            }
        }

        if (StringUtils.isEmpty(subject)) {
            throw IllegalArgumentException("Mail subject can not be empty")
        }

        if (StringUtils.isEmpty(textBody)) {
            throw IllegalArgumentException("Body of the mail can not be empty")
        }
    }
}