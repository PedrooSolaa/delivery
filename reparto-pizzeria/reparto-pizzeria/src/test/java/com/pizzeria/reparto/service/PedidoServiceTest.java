package com.pizzeria.reparto.service;

import com.pizzeria.reparto.model.*;
import com.pizzeria.reparto.repository.PedidoRepository;
import com.pizzeria.reparto.repository.RepartidorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private RepartidorRepository repartidorRepository;
    @Mock private GeocodingService geocodingService;
    @Mock private ProveedorRepartoExternoRegistry proveedorRegistry;
    @Mock private ProveedorRepartoExterno proveedorGlovo;

    private PedidoService pedidoService;

    @BeforeEach
    void crearServicio() {
        pedidoService = new PedidoService(pedidoRepository, repartidorRepository, geocodingService, proveedorRegistry);
    }

    private Pedido pedidoDePrueba() {
        Pedido pedido = new Pedido();
        pedido.setCliente("Cliente de prueba");
        pedido.setEstado(EstadoPedido.EN_PREPARACION);
        return pedido;
    }

    @Test
    void noPermiteEnviarAReparto_sinRepartidorNiProveedor() {
        Pedido pedido = pedidoDePrueba();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.cambiarEstado(1L, EstadoPedido.EN_REPARTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sin repartidor");
    }

    @Test
    void noPermiteEnviarAReparto_siLaEmpresaExternaAunNoAceptaElPedido() {
        Pedido pedido = pedidoDePrueba();
        pedido.setProveedorExterno("Catcher");
        pedido.setEstadoEnvioExterno(EstadoEnvioExterno.PENDIENTE_ACEPTACION);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.cambiarEstado(1L, EstadoPedido.EN_REPARTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("todavia no ha aceptado");
    }

    @Test
    void permiteEnviarAReparto_siLaEmpresaExternaYaAcepto() {
        Pedido pedido = pedidoDePrueba();
        pedido.setProveedorExterno("Catcher");
        pedido.setEstadoEnvioExterno(EstadoEnvioExterno.ACEPTADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.cambiarEstado(1L, EstadoPedido.EN_REPARTO);

        assertThat(resultado.getEstado()).isEqualTo(EstadoPedido.EN_REPARTO);
    }

    @Test
    void permiteEnviarAReparto_siTieneRepartidorPropioAsignado() {
        Pedido pedido = pedidoDePrueba();
        pedido.setRepartidorAsignado(new Repartidor());
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.cambiarEstado(1L, EstadoPedido.EN_REPARTO);

        assertThat(resultado.getEstado()).isEqualTo(EstadoPedido.EN_REPARTO);
    }

    @Test
    void asignarRepartidor_limpiaLosDatosDeUnProveedorExternoAnterior() {
        Pedido pedido = pedidoDePrueba();
        pedido.setProveedorExterno("Glovo");
        pedido.setIdEnvioExterno("GLV-1234");

        Repartidor repartidor = new Repartidor();
        repartidor.setNombre("Ana");

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(repartidorRepository.findById(5L)).thenReturn(Optional.of(repartidor));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.asignarRepartidor(1L, 5L);

        assertThat(resultado.getRepartidorAsignado()).isEqualTo(repartidor);
        assertThat(resultado.getProveedorExterno()).isNull();
        assertThat(resultado.getIdEnvioExterno()).isNull();
    }

    @Test
    void asignarProveedorExterno_guardaLosDatosQueDevuelveLaEmpresa() {
        Pedido pedido = pedidoDePrueba();
        EnvioExterno envio = new EnvioExterno("GLV-ABCD1234", EstadoEnvioExterno.PENDIENTE_ACEPTACION, null, null);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(proveedorRegistry.obtener("Glovo")).thenReturn(Optional.of(proveedorGlovo));
        when(proveedorGlovo.getNombre()).thenReturn("Glovo");
        when(proveedorGlovo.solicitarEnvio(pedido)).thenReturn(envio);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.asignarProveedorExterno(1L, "Glovo");

        assertThat(resultado.getProveedorExterno()).isEqualTo("Glovo");
        assertThat(resultado.getIdEnvioExterno()).isEqualTo("GLV-ABCD1234");
        assertThat(resultado.getEstadoEnvioExterno()).isEqualTo(EstadoEnvioExterno.PENDIENTE_ACEPTACION);
        assertThat(resultado.getRepartidorAsignado()).isNull();
    }

    @Test
    void asignarProveedorExterno_lanzaExcepcion_siElProveedorNoExiste() {
        Pedido pedido = pedidoDePrueba();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(proveedorRegistry.obtener("NoExiste")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.asignarProveedorExterno(1L, "NoExiste"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}