package com.lykke.trade.volume.monitoring.service.spring

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
open class SwaggerConfig {
    @Bean
    open fun apiDocket(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .apiInfo(ApiInfo(
                        "Trade volume monitoring service",
                        "Trade volume monitoring service",
                        "1.0",
                        "",
                        null,
                        "",
                        "",
                        emptyList()
                ))
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lykke.trade.volume.monitoring.web.controllers"))
                .paths(PathSelectors.any())
                .build()

    }
}