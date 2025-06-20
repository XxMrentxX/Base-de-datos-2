package com.tuempresa;

import java.time.LocalDateTime;
import java.time.Duration;

// PARTE 2 - REGISTRAR TIEMPO DE ACTIVIDAD (CASSANDRA)
public class Timer {
    private LocalDateTime inicioSesion;
    private LocalDateTime finSesion;

    public void iniciar() {
        inicioSesion = LocalDateTime.now();
        System.out.println("Usuario inició sesión a las: " + inicioSesion);
    }

    public long parar(){
        inicioSesion = LocalDateTime.now();
        System.out.println("Usuario cerró sesión a las: " + inicioSesion);

        Duration duracion = Duration.between(inicioSesion, finSesion);
        long minutos = duracion.toMinutes();

        return minutos;
    }
}
