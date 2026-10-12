package com.pizzeria.reparto.service;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GlovoProveedorSimulado extends ProveedorSimuladoBase {

    public GlovoProveedorSimulado() {
        super("Glovo", "GLV", 5, 15, 15, 30,
                List.of("Carlos M.", "Lucía R.", "Andrés P.", "Marta S."));
    }
}