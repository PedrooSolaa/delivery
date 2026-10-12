package com.pizzeria.reparto.controller;

import com.pizzeria.reparto.model.Repartidor;
import com.pizzeria.reparto.repository.RepartidorRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repartidores")
public class RepartidorController {

    private final RepartidorRepository repartidorRepository;

    public RepartidorController(RepartidorRepository repartidorRepository) {
        this.repartidorRepository = repartidorRepository;
    }

    @GetMapping
    public List<Repartidor> listar() {
        return repartidorRepository.findAll();
    }

    @PostMapping
    public Repartidor crear(@RequestBody Repartidor repartidor) {
        return repartidorRepository.save(repartidor);
    }
}