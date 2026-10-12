package com.pizzeria.reparto.service;

import com.pizzeria.reparto.model.OrigenPedido;
import com.pizzeria.reparto.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ImportadorPedidosCarloGest implements ImportadorPedidosExternos {

    private static final Logger log = LoggerFactory.getLogger(ImportadorPedidosCarloGest.class);

    private final JdbcTemplate firebirdJdbcTemplate;
    private final PedidoService pedidoService;

    @Value("${importador.carlogest.habilitado:true}")
    private boolean habilitado;

    public ImportadorPedidosCarloGest(
            @Qualifier("firebirdJdbcTemplate") JdbcTemplate firebirdJdbcTemplate,
            PedidoService pedidoService) {
        this.firebirdJdbcTemplate = firebirdJdbcTemplate;
        this.pedidoService = pedidoService;
    }

    @Override
    @Scheduled(fixedDelay = 5000)
    public void importarPedidosNuevos() {
        if (!habilitado) {
            return;
        }

        String sqlSeleccionar =
                "SELECT id, numero_visible, cliente, telefono, direccion, detalle_pedido " +
                        "FROM pedidos_carlogest " +
                        "WHERE tipo = 'DOMICILIO' AND exportado_web = 0";

        List<Map<String, Object>> filas = firebirdJdbcTemplate.queryForList(sqlSeleccionar);

        if (filas.isEmpty()) {
            log.debug("No hay pedidos de domicilio nuevos en CarloGest.");
            return;
        }

        log.info("Encontrados {} pedido(s) de domicilio nuevos en CarloGest.", filas.size());

        for (Map<String, Object> fila : filas) {
            Long idCarloGest = ((Number) fila.get("id")).longValue();

            Pedido pedido = new Pedido();
            pedido.setCliente((String) fila.get("cliente"));
            pedido.setTelefono((String) fila.get("telefono"));
            pedido.setDireccion((String) fila.get("direccion"));
            pedido.setDetallePedido((String) fila.get("detalle_pedido"));
            pedido.setOrigen(OrigenPedido.TELEFONO);

            pedidoService.crear(pedido);

            firebirdJdbcTemplate.update(
                    "UPDATE pedidos_carlogest SET exportado_web = 1 WHERE id = ?",
                    idCarloGest);

            log.info("Pedido CarloGest id={} (numero {}) importado correctamente.",
                    idCarloGest, fila.get("numero_visible"));
        }
    }
}