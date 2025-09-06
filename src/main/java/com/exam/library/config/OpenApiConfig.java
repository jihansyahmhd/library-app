package com.exam.library.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;
    @Value("${spring.webflux.base-path:/api/v1}")
    private String basePath;

    @Bean
    public OpenAPI libraryManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Management System API")
                        .description("RESTful API for managing a library system with Spring Boot Reactive")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Library System Developer")
                                .email("developer@library.com")
                                .url("https://github.com/library-system"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort+basePath)
                                .description("Local Development Server"),
                        new Server()
                                .url("http://library-api:8080"+basePath)
                                .description("Docker Container Server")
                ));
    }
}