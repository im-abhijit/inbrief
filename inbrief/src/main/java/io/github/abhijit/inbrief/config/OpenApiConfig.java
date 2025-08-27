package io.github.abhijit.inbrief.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inbriefOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inbrief API")
                        .version("v1")
                        .description("REST endpoints for news retrieval, search, trending, and LLM-assisted query orchestration."));
    }
}

