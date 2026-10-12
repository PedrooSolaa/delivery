package com.pizzeria.reparto.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class FirebirdConfig {

    @Value("${firebird.datasource.url}")
    private String url;

    @Value("${firebird.datasource.username}")
    private String username;

    @Value("${firebird.datasource.password}")
    private String password;

    @Value("${firebird.datasource.driver-class-name}")
    private String driverClassName;

    // Usamos DriverManagerDataSource (sin pool de conexiones, a diferencia de la
    // conexion principal a MySQL que usa Hikari). Es mas simple, y como aqui solo
    // hacemos una consulta cada cierto tiempo (no cientos por segundo), no hace
    // falta un pool de conexiones para esto.
    @Bean(name = "firebirdDataSource")
    public DataSource firebirdDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    // JdbcTemplate es una forma mas directa de hacer SQL que JPA: en vez de mapear
    // esta tabla externa a una @Entity nuestra (que implicaria que Hibernate
    // intentaria gestionarla como si fuera nuestra), simplemente lanzamos
    // consultas SQL y leemos el resultado fila a fila. Tiene mas sentido aqui
    // porque esta tabla no es "nuestra", es de un sistema externo que no controlamos.
    @Bean(name = "firebirdJdbcTemplate")
    public JdbcTemplate firebirdJdbcTemplate() {
        return new JdbcTemplate(firebirdDataSource());
    }
}
