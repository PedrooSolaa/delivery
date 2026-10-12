package com.pizzeria.reparto.service;

import com.pizzeria.reparto.model.EstadoEnvioExterno;
import com.pizzeria.reparto.model.EstadoPedido;
import com.pizzeria.reparto.model.Pedido;
import com.pizzeria.reparto.model.Repartidor;
import com.pizzeria.reparto.repository.PedidoRepository;
import com.pizzeria.reparto.repository.RepartidorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final RepartidorRepository repartidorRepository;
    private final GeocodingService geocodingService;
    private final ProveedorRepartoExternoRegistry proveedorRegistry;

    public PedidoService(
            PedidoRepository pedidoRepository,
            RepartidorRepository repartidorRepository,
            GeocodingService geocodingService,
            ProveedorRepartoExternoRegistry proveedorRegistry) {
        this.pedidoRepository = pedidoRepository;
        this.repartidorRepository = repartidorRepository;
        this.geocodingService = geocodingService;
        this.proveedorRegistry = proveedorRegistry;
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> listarPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado);
    }

    public List<Pedido> listarPendientesOrdenados() {
        return pedidoRepository.findByEstadoOrderByFechaCreacionAsc(EstadoPedido.PENDIENTE);
    }

    public List<Pedido> listarRecientes(int horasAtras) {
        LocalDateTime desde = LocalDateTime.now().minusHours(horasAtras);
        return pedidoRepository.findByFechaCreacionAfterOrderByFechaCreacionDesc(desde);
    }

    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));
    }

    public Pedido crear(Pedido pedido) {
        geocodingService.geocodificar(pedido.getDireccion()).ifPresent(coords -> {
            pedido.setLatitud(coords.latitud());
            pedido.setLongitud(coords.longitud());
        });

        if (pedido.getLatitud() != null && pedido.getLongitud() != null) {
            geocodingService.obtenerCoordenadasNegocio().ifPresent(negocio -> {
                double distancia = geocodingService.distanciaEnKm(
                        negocio.latitud(), negocio.longitud(),
                        pedido.getLatitud(), pedido.getLongitud());
                pedido.setDistanciaKm(Math.round(distancia * 10) / 10.0);
            });
        }

        return pedidoRepository.save(pedido);
    }

    public Pedido asignarRepartidor(Long pedidoId, Long repartidorId) {
        Pedido pedido = obtenerPorId(pedidoId);
        Repartidor repartidor = repartidorRepository.findById(repartidorId)
                .orElseThrow(() -> new RuntimeException("Repartidor no encontrado: " + repartidorId));
        pedido.setRepartidorAsignado(repartidor);
        limpiarDatosEnvioExterno(pedido);
        return pedidoRepository.save(pedido);
    }

    public Pedido asignarProveedorExterno(Long pedidoId, String nombreProveedor) {
        Pedido pedido = obtenerPorId(pedidoId);

        ProveedorRepartoExterno proveedor = proveedorRegistry.obtener(nombreProveedor)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Proveedor externo desconocido: " + nombreProveedor));

        EnvioExterno envio = proveedor.solicitarEnvio(pedido);

        pedido.setRepartidorAsignado(null);
        pedido.setProveedorExterno(proveedor.getNombre());
        pedido.setIdEnvioExterno(envio.idEnvio());
        pedido.setEstadoEnvioExterno(envio.estado());
        pedido.setNombreRepartidorExterno(envio.nombreRepartidor());
        pedido.setTiempoEstimadoExternoMinutos(envio.tiempoEstimadoMinutos());
        pedido.setFechaSolicitudExterno(LocalDateTime.now());

        return pedidoRepository.save(pedido);
    }

    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = obtenerPorId(id);

        if (nuevoEstado == EstadoPedido.EN_REPARTO) {
            boolean tieneAsignacion = pedido.getRepartidorAsignado() != null
                    || pedido.getProveedorExterno() != null;

            if (!tieneAsignacion) {
                throw new IllegalStateException(
                        "No se puede enviar a reparto el pedido " + id
                                + " sin repartidor o empresa externa asignados.");
            }

            // Con una empresa externa, el pedido sale a reparto cuando uno de
            // sus repartidores ya lo ha aceptado (es quien viene a recogerlo).
            if (pedido.getEstadoEnvioExterno() == EstadoEnvioExterno.PENDIENTE_ACEPTACION) {
                throw new IllegalStateException(
                        pedido.getProveedorExterno() + " todavia no ha aceptado el pedido. "
                                + "Espera a que un repartidor lo acepte.");
            }
        }

        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    // Lo llama el SincronizadorEnviosExternos cada pocos segundos: pregunta a
    // cada empresa por los envios pendientes de aceptacion y, si ya los han
    // aceptado, guarda el nombre del repartidor y el tiempo estimado.
    public void sincronizarEnviosExternos() {
        List<Pedido> pendientes = pedidoRepository.findByEstadoEnvioExterno(
                EstadoEnvioExterno.PENDIENTE_ACEPTACION);

        for (Pedido pedido : pendientes) {
            proveedorRegistry.obtener(pedido.getProveedorExterno()).ifPresent(proveedor -> {
                EnvioExterno actual = proveedor.consultarEnvio(pedido);
                if (actual.estado() == EstadoEnvioExterno.ACEPTADO) {
                    pedido.setEstadoEnvioExterno(EstadoEnvioExterno.ACEPTADO);
                    pedido.setNombreRepartidorExterno(actual.nombreRepartidor());
                    pedido.setTiempoEstimadoExternoMinutos(actual.tiempoEstimadoMinutos());
                    pedidoRepository.save(pedido);
                }
            });
        }
    }

    private void limpiarDatosEnvioExterno(Pedido pedido) {
        pedido.setProveedorExterno(null);
        pedido.setIdEnvioExterno(null);
        pedido.setEstadoEnvioExterno(null);
        pedido.setNombreRepartidorExterno(null);
        pedido.setTiempoEstimadoExternoMinutos(null);
        pedido.setFechaSolicitudExterno(null);
    }
}