package com.pizzeria.reparto.model;

// El ciclo de vida de un pedido de domicilio
public enum EstadoPedido {
    PENDIENTE,       // recien llegado, nadie lo ha atendido
    EN_PREPARACION,  // se esta cocinando
    EN_REPARTO,      // ya salio con un repartidor
    ENTREGADO,
    CANCELADO
}
