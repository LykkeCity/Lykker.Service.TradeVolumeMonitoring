package com.lykke.trade.volume.monitoring.service.spring

import com.lykke.trade.volume.monitoring.service.config.Config
import com.lykke.utils.config.ConfigInitializer
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

@Configuration
open class ApplicationConfig {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ApplicationConfig::class.java.name)
    }

    @Bean
    open fun config(environment: Environment): Config? {
        if (environment.acceptsProfiles(Profiles.of("local_config"))) {
            return ConfigInitializer.initConfig("local", classOfT = Config::class.java)
        }

        return if(environment.acceptsProfiles(Profiles.of("default"))) {
            val commandLineArgs = environment.getProperty("nonOptionArgs", Array<String>::class.java)
            if (commandLineArgs == null) {
                val errorMessage = "Not enough args. Usage: httpConfigString"
                LOGGER.error(errorMessage)
                throw IllegalArgumentException(errorMessage)
            }
            ConfigInitializer.initConfig(commandLineArgs[0], classOfT = Config::class.java)
        } else {
            null
        }
    }

}