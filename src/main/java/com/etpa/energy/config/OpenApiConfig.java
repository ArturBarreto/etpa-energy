package com.etpa.energy.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Energy API",
                version = "1.0.0",
                description = "A Spring Boot 3 / Java 21 application for managing load profiles, meters," +
                        " cumulative readings, and deriving monthly consumption. This API was built as part" +
                        " of the ETPA Energy use-case assignment." ,
                contact = @Contact(name = "Artur Gomes Barreto", email = "artur.gomes.barreto@gmail.com")
        ),
        servers = {@Server(url = "/api", description = "Default server")},
        tags = {
                @Tag(name = "Profiles", description = "Create/update and list load profiles"),
                @Tag(name = "Meters", description = "Manage meters and their profile link"),
                @Tag(name = "Readings", description = "Import and fetch cumulative meter readings"),
                @Tag(name = "Consumption", description = "Derived monthly consumption")
        }
)
@Configuration
public class OpenApiConfig {

}
