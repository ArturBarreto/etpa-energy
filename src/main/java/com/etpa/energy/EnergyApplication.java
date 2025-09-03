package com.etpa.energy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point. Exposes REST endpoints under /api (see application.yml context-path).
 */
@SpringBootApplication
public class EnergyApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnergyApplication.class, args);
    }
}
