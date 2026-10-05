package com.tiendalego.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tiendaLegoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tienda LEGO - API REST")
                        .description("API REST para la tienda en linea modular especializada en sets "
                                + "y coleccionables de LEGO. Permite gestionar categorias, productos "
                                + "y carrito de compras.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo Tienda LEGO")
                                .email("equipo@tiendalego.com")));
    }
}
