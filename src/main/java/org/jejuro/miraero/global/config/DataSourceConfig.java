package org.jejuro.miraero.global.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@PropertySource({
    "classpath:application.properties",
    "classpath:application-${spring.profiles.active:local}.properties"
})
public class DataSourceConfig {

  private final Environment environment;

  public DataSourceConfig(Environment environment) {
    this.environment = environment;
  }

  @Bean(destroyMethod = "close")
  public HikariDataSource dataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(environment.getRequiredProperty("spring.datasource.url"));
    config.setUsername(environment.getRequiredProperty("spring.datasource.username"));
    config.setPassword(environment.getRequiredProperty("spring.datasource.password"));
    config.setDriverClassName(
        environment.getRequiredProperty("spring.datasource.driver-class-name"));
    return new HikariDataSource(config);
  }

  @Bean
  public PlatformTransactionManager transactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }
}
