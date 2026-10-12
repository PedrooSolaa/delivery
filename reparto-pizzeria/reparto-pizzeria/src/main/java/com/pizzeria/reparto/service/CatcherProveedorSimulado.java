package com.pizzeria.reparto.service;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatcherProveedorSimulado extends ProveedorSimuladoBase {

    public CatcherProveedorSimulado() {
        super("Catcher", "CAT", 15, 40, 25, 40,
                List.of("Rubén A.", "Nuria C.", "Diego F.", "Irene V."));
    }
}