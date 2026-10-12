package com.pizzeria.reparto.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SincronizadorEnviosExternos {

    private final PedidoService pedidoService;

    public SincronizadorEnviosExternos(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @Scheduled(fixedDelay = 5000)
    public void sincronizar() {
        pedidoService.sincronizarEnviosExternos();
    }
}