package com.pizzeria.reparto.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProveedorRepartoExternoRegistry {

    private final Map<String, ProveedorRepartoExterno> proveedoresPorNombre;

    public ProveedorRepartoExternoRegistry(List<ProveedorRepartoExterno> proveedores) {
        this.proveedoresPorNombre = proveedores.stream()
                .collect(Collectors.toMap(ProveedorRepartoExterno::getNombre, Function.identity()));
    }

    public Optional<ProveedorRepartoExterno> obtener(String nombre) {
        return Optional.ofNullable(proveedoresPorNombre.get(nombre));
    }
}