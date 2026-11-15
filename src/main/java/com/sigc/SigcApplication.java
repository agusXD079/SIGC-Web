package com.sigc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de arranque de SIGC (Sistema Integral de Gestion de Concesionaria). */
@SpringBootApplication
public class SigcApplication {
    /**
     * @param args argumentos de linea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(SigcApplication.class, args);
    }
}
