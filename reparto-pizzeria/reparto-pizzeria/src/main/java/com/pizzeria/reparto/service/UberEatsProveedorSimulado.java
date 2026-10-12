package com.pizzeria.reparto.service;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UberEatsProveedorSimulado extends ProveedorSimuladoBase {

    public UberEatsProveedorSimulado() {
        super("Uber Eats", "UBE", 10, 25, 20, 35,
                List.of("Javier L.", "Elena G.", "Sergio T.", "Paula D."));
    }
}