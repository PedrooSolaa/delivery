package com.pizzeria.reparto.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeocodingServiceTest {

    private final GeocodingService geocodingService = new GeocodingService();

    @Test
    void distanciaEntreElMismoPuntoEsCero() {
        double distancia = geocodingService.distanciaEnKm(39.8628, -4.0273, 39.8628, -4.0273);

        assertThat(distancia).isEqualTo(0.0);
    }

    @Test
    void calculaLaDistanciaAproximadaEntreMadridYToledo() {
        // Puerta del Sol (Madrid) y el centro de Toledo.
        double distancia = geocodingService.distanciaEnKm(40.4168, -3.7038, 39.8628, -4.0273);

        // En linea recta son aprox. 68-70 km; dejamos margen porque no es el
        // objetivo del test clavar el decimal exacto, sino que el calculo
        // sea razonable.
        assertThat(distancia).isBetween(60.0, 80.0);
    }
}