package com.pizzeria.reparto.controller;

import com.pizzeria.reparto.model.EstadoPedido;
import com.pizzeria.reparto.model.Pedido;
import com.pizzeria.reparto.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<Pedido> listar(@RequestParam(required = false) EstadoPedido estado) {
        if (estado != null) {
            return pedidoService.listarPorEstado(estado);
        }
        return pedidoService.listarTodos();
    }

    @GetMapping("/pendientes")
    public List<Pedido> listarPendientes() {
        return pedidoService.listarPendientesOrdenados();
    }

    @GetMapping("/recientes")
    public List<Pedido> listarRecientes(@RequestParam(defaultValue = "24") int horas) {
        return pedidoService.listarRecientes(horas);
    }

    @GetMapping("/{id}")
    public Pedido obtener(@PathVariable Long id) {
        return pedidoService.obtenerPorId(id);
    }

    @PostMapping
    public Pedido crear(@Valid @RequestBody Pedido pedido) {
        return pedidoService.crear(pedido);
    }

    @PatchMapping("/{id}/repartidor")
    public Pedido asignarRepartidor(@PathVariable Long id, @RequestParam Long repartidorId) {
        return pedidoService.asignarRepartidor(id, repartidorId);
    }

    @PatchMapping("/{id}/proveedor-externo")
    public Pedido asignarProveedorExterno(@PathVariable Long id, @RequestParam String nombre) {
        return pedidoService.asignarProveedorExterno(id, nombre);
    }

    @PatchMapping("/{id}/estado")
    public Pedido cambiarEstado(@PathVariable Long id, @RequestParam EstadoPedido nuevoEstado) {
        return pedidoService.cambiarEstado(id, nuevoEstado);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> manejarEstadoInvalido(IllegalStateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> manejarProveedorInvalido(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}