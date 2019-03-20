package com.lykke.trade.volume.monitoring.service.spring

import com.lykke.trade.volume.monitoring.service.config.Config
import com.lykke.trade.volume.monitoring.service.entity.impl.TradeVolumeCacheImpl
import com.lykke.utils.config.ConfigInitializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.core.env.get
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@Configuration
@EnableScheduling
open class ApplicationConfig {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ApplicationConfig::class.java.name)
    }


    @Autowired
    private lateinit var environment: Environment

    @Bean
    open fun taskScheduler(): TaskScheduler {
        val threadPoolTaskScheduler = ThreadPoolTaskScheduler()
        threadPoolTaskScheduler.setThreadNamePrefix("scheduled-task-")
        threadPoolTaskScheduler.poolSize = environment["concurrent.scheduler.pool.size"]!!.toInt()
        return threadPoolTaskScheduler
    }

    @Bean(name = ["Config"])
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

    fun tradeVolumeCache(config: Config): TradeVolumeCacheImpl {
        return TradeVolumeCacheImpl(config.tradeVolumeConfig.tradeVolumeCacheConfig.volumePeriod,
                config.tradeVolumeConfig.tradeVolumeCacheConfig.expiryRatio)
    }

}