package com.pizzeria.reparto.service;

import com.pizzeria.reparto.model.Pedido;

public interface ProveedorRepartoExterno {

    String getNombre();

    // Pide el envio. La empresa responde con un id de seguimiento y el envio
    // queda pendiente hasta que uno de sus repartidores lo acepte.
    EnvioExterno solicitarEnvio(Pedido pedido);

    // Pregunta como va un envio ya solicitado. Con una API real seria una
    // llamada HTTP (o llegaria por webhook); aqui lo simulamos.
    EnvioExterno consultarEnvio(Pedido pedido);
}