package com.pizzeria.reparto.service;

import com.pizzeria.reparto.model.EstadoEnvioExterno;

// Lo que nos dice una empresa externa sobre un envio, tanto al solicitarlo
// (estado PENDIENTE_ACEPTACION, sin repartidor) como al consultarlo despues
// (ACEPTADO, ya con nombre de repartidor y tiempo estimado).
public record EnvioExterno(
        String idEnvio,
        EstadoEnvioExterno estado,
        String nombreRepartidor,
        Integer tiempoEstimadoMinutos) {}