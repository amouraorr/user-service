package com.fiap.userservice.infrastructure.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PÓS GRADUAÇÃO - FIAP 2025 - SERVIÇO DE USUÁRIOS")
                        .version("1.0.0")
                        .description("Microsserviço responsável pelo cadastro e gerenciamento de usuários (moradores e funcionários), armazenando informações de contato e apartamento, e fornecendo APIs para gerenciamento de perfis."));
    }
}