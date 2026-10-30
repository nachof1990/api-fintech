package com.diplomatura.apifintech.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Prepara el cliente HTTP usado para consultar cotizaciones.
 */
@Configuration
public class CotizacionConfig {

    /**
     * Crea un cliente con dirección configurable y tiempos de espera.
     *
     * @param baseUrl dirección del proveedor externo
     * @return cliente HTTP que Spring podrá inyectar en el servicio
     */
    @Bean
    public RestClient dolarRestClient(
            @Value("${cotizacion.api.base-url}") String baseUrl) {

        // Limita el tiempo para establecer la conexión.
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        // Limita la espera de respuesta para cada solicitud.
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
