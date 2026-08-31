package org.jejuro.miraero.global.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Configuration
@EnableScheduling
@ComponentScan(
    basePackages = {
        "org.jejuro.miraero.domain",
        "org.jejuro.miraero.global"
    },
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ANNOTATION,
            classes = {
                Configuration.class,
                Controller.class,
                RestControllerAdvice.class
            }
        )
    }
)
@Import({
    DataSourceConfig.class,
    MyBatisConfig.class,
    SecurityConfig.class,
    RedisConfig.class,
    FlywayConfig.class,
    AsyncConfig.class
})
public class RootConfig {

}
