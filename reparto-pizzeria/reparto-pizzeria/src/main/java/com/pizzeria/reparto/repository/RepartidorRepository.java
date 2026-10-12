package com.pizzeria.reparto.repository;

import com.pizzeria.reparto.model.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepartidorRepository extends JpaRepository<Repartidor, Long> {
}