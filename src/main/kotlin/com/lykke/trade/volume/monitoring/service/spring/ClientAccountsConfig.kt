package com.lykke.trade.volume.monitoring.service.spring

import com.lykke.client.accounts.ClientAccountCacheFactory
import com.lykke.client.accounts.ClientAccountsCache
import com.lykke.client.accounts.config.RabbitMqConfig
import com.lykke.trade.volume.monitoring.service.LOGGER
import com.lykke.trade.volume.monitoring.service.config.Config
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import javax.annotation.PreDestroy
import com.lykke.client.accounts.config.Config as ClientsAccountsConfig
import com.lykke.client.accounts.config.HttpConfig as ClientsAccountsHttpConfig

@Configuration
open class ClientAccountsConfig {

    @Autowired
    private lateinit var config: Config

    @Bean
    open fun clientAccountEventsQueue(): BlockingQueue<ByteArray> {
        return LinkedBlockingQueue<ByteArray>()
    }

    @Bean
    open fun clientAccountsCache(): ClientAccountsCache {
        val clientAccountsConfig = config.tradeVolumeConfig.clientAccountsConfig
        return ClientAccountCacheFactory.get(ClientsAccountsConfig(RabbitMqConfig(uri = clientAccountsConfig.rmqConfig.uri,
                exchange = clientAccountsConfig.rmqConfig.exchange,
                queueName = clientAccountsConfig.rmqConfig.queueName,
                routingKey = clientAccountsConfig.rmqConfig.routingKey,
                queue = clientAccountEventsQueue()),
                ClientsAccountsHttpConfig(config.tradeVolumeConfig.clientAccountsConfig.httpConfig.baseUrl,
                        config.tradeVolumeConfig.clientAccountsConfig.httpConfig.timeout)))
    }

    @PreDestroy
    fun destroy() {
        try {
            ClientAccountCacheFactory.shutdownAll()
        } catch (e: Exception) {
            LOGGER.error("Error occurred on client accounts cache shutdown", e)
        }
    }
}