package com.pizzeria.reparto.service;

import com.pizzeria.reparto.model.EstadoEnvioExterno;
import com.pizzeria.reparto.model.Pedido;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Base comun de los proveedores SIMULADOS. La "decision" de cuanto tarda en
// aceptar y que repartidor lo acepta se calcula a partir del id del envio, asi
// que es siempre la misma cada vez que se consulta y no hace falta guardar
// estado en memoria.
public abstract class ProveedorSimuladoBase implements ProveedorRepartoExterno {

    private final String nombre;
    private final String prefijo;
    private final int minSegundosAceptacion;
    private final int maxSegundosAceptacion;
    private final int minEtaMinutos;
    private final int maxEtaMinutos;
    private final List<String> repartidores;

    protected ProveedorSimuladoBase(
            String nombre,
            String prefijo,
            int minSegundosAceptacion,
            int maxSegundosAceptacion,
            int minEtaMinutos,
            int maxEtaMinutos,
            List<String> repartidores) {
        this.nombre = nombre;
        this.prefijo = prefijo;
        this.minSegundosAceptacion = minSegundosAceptacion;
        this.maxSegundosAceptacion = maxSegundosAceptacion;
        this.minEtaMinutos = minEtaMinutos;
        this.maxEtaMinutos = maxEtaMinutos;
        this.repartidores = repartidores;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public EnvioExterno solicitarEnvio(Pedido pedido) {
        String idEnvio = prefijo + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new EnvioExterno(idEnvio, EstadoEnvioExterno.PENDIENTE_ACEPTACION, null, null);
    }

    @Override
    public EnvioExterno consultarEnvio(Pedido pedido) {
        String idEnvio = pedido.getIdEnvioExterno();
        LocalDateTime solicitud = pedido.getFechaSolicitudExterno();

        if (idEnvio == null || solicitud == null) {
            return new EnvioExterno(idEnvio, EstadoEnvioExterno.PENDIENTE_ACEPTACION, null, null);
        }

        int semilla = Math.floorMod(idEnvio.hashCode(), Integer.MAX_VALUE);
        int segundosParaAceptar = minSegundosAceptacion
                + (semilla % (maxSegundosAceptacion - minSegundosAceptacion + 1));
        long segundosTranscurridos = Duration.between(solicitud, LocalDateTime.now()).getSeconds();

        if (segundosTranscurridos < segundosParaAceptar) {
            return new EnvioExterno(idEnvio, EstadoEnvioExterno.PENDIENTE_ACEPTACION, null, null);
        }

        String nombreRepartidor = repartidores.get(semilla % repartidores.size());
        int eta = minEtaMinutos + ((semilla / 7) % (maxEtaMinutos - minEtaMinutos + 1));
        return new EnvioExterno(idEnvio, EstadoEnvioExterno.ACEPTADO, nombreRepartidor, eta);
    }
}