package com.lykke.trade.volume.monitoring.service.spring

import com.lykke.trade.volume.monitoring.service.config.Config
import com.lykke.utils.config.ConfigInitializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.FactoryBean
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Component

@Component("Config")
class ConfigFactory(private val environment: Environment) : FactoryBean<Config> {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ConfigFactory::class.java.name)
        private const val LOCAL_CONFIG_PROFILE = "local_config"
        private var config: Config? = null

        @Synchronized
        fun getConfig(environment: Environment): Config {
            if (config == null) {
                config = ConfigInitializer.initConfig(getConfigUrl(environment), classOfT = Config::class.java)
            }
            return config as Config
        }

        private fun getConfigUrl(environment: Environment): String {
            return if (environment.acceptsProfiles(Profiles.of(LOCAL_CONFIG_PROFILE)))
                "local"
            else {
                val commandLineArgs = environment.getProperty("nonOptionArgs", Array<String>::class.java)
                if (commandLineArgs == null) {
                    val errorMessage = "Not enough args. Usage: httpConfigString"
                    LOGGER.error(errorMessage)
                    throw IllegalArgumentException(errorMessage)
                }
                commandLineArgs[0]
            }
        }
    }

    override fun getObject(): Config? {
        return getConfig(environment)
    }

    override fun getObjectType(): Class<*>? {
        return Config::class.java
    }
}