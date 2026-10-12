package com.pizzeria.reparto.controller;

import com.pizzeria.reparto.service.GeocodingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/configuracion")
public class ConfiguracionController {

    @Value("${negocio.nombre}")
    private String nombreNegocio;

    @Value("${negocio.direccion}")
    private String direccionNegocio;

    private final GeocodingService geocodingService;

    public ConfiguracionController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    public record ConfiguracionNegocio(
            String nombreNegocio,
            String direccionNegocio,
            Double latitud,
            Double longitud) {}

    @GetMapping
    public ConfiguracionNegocio obtener() {
        var coords = geocodingService.obtenerCoordenadasNegocio();
        Double lat = coords.map(GeocodingService.Coordenadas::latitud).orElse(null);
        Double lon = coords.map(GeocodingService.Coordenadas::longitud).orElse(null);
        return new ConfiguracionNegocio(nombreNegocio, direccionNegocio, lat, lon);
    }
}