package com.exam.library.config;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Slf4j
@Configuration
@EnableConfigurationProperties({R2dbcProperties.class, FlywayProperties.class})
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    @Value("${spring.flyway.url:jdbc:postgresql://localhost:5432/library_db}")
    private String flywayUrl;

    @Value("${spring.flyway.user:library_user}")
    private String flywayUser;

    @Value("${spring.flyway.password:library_pass}")
    private String flywayPassword;

    // R2DBC configuration properties with default values
    @Value("${spring.r2dbc.username:library_user}")
    private String r2dbcUsername;

    @Value("${spring.r2dbc.password:library_pass}")
    private String r2dbcPassword;

    @Value("${spring.r2dbc.name:library_db}")
    private String databaseName;

    @Value("${spring.r2dbc.host:localhost}")
    private String host;

    @Value("${spring.r2dbc.port:5432}")
    private int port;

    @Bean
    @ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true")
    public Flyway flyway() {
        log.info("Configuring Flyway with URL: {}", flywayUrl);

        Flyway flyway = Flyway.configure()
                .dataSource(flywayUrl, flywayUser, flywayPassword)
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .locations("classpath:db/migration")
                .load();

        flyway.migrate(); // <-- ini yang bikin Flyway jalan
        return flyway;
    }


    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        return initializer;
    }

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        log.info("Configuring R2DBC ConnectionFactory for host: {}, port: {}, database: {}",
                host, port, databaseName);

        try {
            ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
                    .option(DRIVER, "postgresql")
                    .option(HOST, host)
                    .option(PORT, port)
                    .option(USER, r2dbcUsername)
                    .option(PASSWORD, r2dbcPassword)
                    .option(DATABASE, databaseName)
                    .option(CONNECT_TIMEOUT, java.time.Duration.ofSeconds(30))
                    .option(STATEMENT_TIMEOUT, java.time.Duration.ofSeconds(30))
                    // Additional PostgreSQL specific options
                    .option(ConnectionFactoryOptions.SSL, false)
                    .build();

            ConnectionFactory connectionFactory = io.r2dbc.spi.ConnectionFactories.get(options);
            log.info("R2DBC ConnectionFactory configured successfully");
            return connectionFactory;

        } catch (Exception e) {
            log.error("Failed to configure R2DBC ConnectionFactory: {}", e.getMessage(), e);

            // Fallback configuration attempt
            log.info("Attempting fallback R2DBC configuration...");
            try {
                String fallbackUrl = String.format("r2dbc:postgresql://%s:%d/%s", host, port, databaseName);
                ConnectionFactory fallbackFactory = io.r2dbc.spi.ConnectionFactories.get(fallbackUrl);
                log.info("Fallback R2DBC ConnectionFactory configured successfully");
                return fallbackFactory;
            } catch (Exception fallbackException) {
                log.error("Fallback R2DBC configuration also failed: {}", fallbackException.getMessage());
                throw new RuntimeException("Both primary and fallback database connection configurations failed", e);
            }
        }
    }
}