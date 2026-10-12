package com.pizzeria.reparto.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);

    private static final String USER_AGENT = "RepartoPizzeriaApp/1.0 (proyecto de portfolio)";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${negocio.direccion}")
    private String direccionNegocio;

    @Value("${opencage.api.key}")
    private String opencageApiKey;

    private Coordenadas coordenadasNegocio;

    public record Coordenadas(double latitud, double longitud) {}

    public Optional<Coordenadas> geocodificar(String direccion) {
        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl("https://api.opencagedata.com/geocode/v1/json")
                    .queryParam("q", direccion)
                    .queryParam("key", opencageApiKey)
                    .queryParam("limit", 1)
                    .queryParam("countrycode", "es")
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            HttpEntity<Void> peticion = new HttpEntity<>(headers);

            ResponseEntity<String> respuesta =
                    restTemplate.exchange(uri, HttpMethod.GET, peticion, String.class);

            log.info("OpenCage status: {} para direccion: {}", respuesta.getStatusCode(), direccion);

            JsonNode raiz = objectMapper.readTree(respuesta.getBody());
            JsonNode resultados = raiz.get("results");
            if (resultados == null || !resultados.isArray() || resultados.isEmpty()) {
                log.warn("No se encontraron coordenadas para: {} | respuesta de OpenCage: {}",
                        direccion, respuesta.getBody());
                return Optional.empty();
            }

            JsonNode geometria = resultados.get(0).get("geometry");
            double lat = geometria.get("lat").asDouble();
            double lon = geometria.get("lng").asDouble();
            return Optional.of(new Coordenadas(lat, lon));

        } catch (Exception e) {
            log.error("Error geocodificando '{}': {}", direccion, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Coordenadas> obtenerCoordenadasNegocio() {
        if (coordenadasNegocio == null) {
            Optional<Coordenadas> resultado = geocodificar(direccionNegocio);
            if (resultado.isEmpty()) {
                log.error("No se pudo geocodificar la direccion del negocio: {}", direccionNegocio);
                return Optional.empty();
            }
            coordenadasNegocio = resultado.get();
        }
        return Optional.of(coordenadasNegocio);
    }

    public double distanciaEnKm(double lat1, double lon1, double lat2, double lon2) {
        final double RADIO_TIERRA_KM = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIO_TIERRA_KM * c;
    }
}