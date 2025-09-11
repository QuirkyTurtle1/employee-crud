package org.example.web.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition (
        info = @Info(
                title = "Employee CRUD API",
                version = "v1",
                description = "Training project: CRUD for clients, products, orders."
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local"),
                @Server(url = "http://localhost:8090", description = "Local Alt")
        },
        tags = {
                @Tag(name = "Products", description = "Product management"),
                @Tag(name = "Clients",  description = "Client management"),
                @Tag(name = "Orders",   description = "Order management")
        }
)
public class OpenApiConfig {

}
