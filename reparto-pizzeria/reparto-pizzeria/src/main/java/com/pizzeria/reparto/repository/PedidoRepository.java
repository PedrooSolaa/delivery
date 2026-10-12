package com.pizzeria.reparto.repository;

import com.pizzeria.reparto.model.EstadoEnvioExterno;
import com.pizzeria.reparto.model.EstadoPedido;
import com.pizzeria.reparto.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEstadoOrderByFechaCreacionAsc(EstadoPedido estado);

    List<Pedido> findByEstado(EstadoPedido estado);

    List<Pedido> findByFechaCreacionAfterOrderByFechaCreacionDesc(LocalDateTime desde);

    // Los pedidos asignados a una empresa externa que aun no ha aceptado el envio.
    List<Pedido> findByEstadoEnvioExterno(EstadoEnvioExterno estado);
}