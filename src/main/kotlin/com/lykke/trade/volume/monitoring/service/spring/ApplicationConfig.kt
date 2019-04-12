package com.lykke.trade.volume.monitoring.service.spring

import com.lykke.utils.AppInitializer
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import javax.annotation.PostConstruct

@Configuration
@EnableScheduling
class ApplicationConfig : EnvironmentAware {

    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    @Bean
    fun taskScheduler(): TaskScheduler {
        val threadPoolTaskScheduler = ThreadPoolTaskScheduler()
        threadPoolTaskScheduler.setThreadNamePrefix("scheduled-task-")
        threadPoolTaskScheduler.poolSize = environment["concurrent.scheduler.pool.size"]!!.toInt()
        return threadPoolTaskScheduler
    }

    @PostConstruct
    fun initApplication() {
        AppInitializer.init()
    }

}