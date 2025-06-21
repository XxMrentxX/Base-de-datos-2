package com.tuempresa;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.LocalTime;

// PARTE 2 - REGISTRAR TIEMPO DE ACTIVIDAD (CASSANDRA)
public class Timer {
    private LocalDateTime inicioSesion;

    public void iniciar() {
        this.inicioSesion = LocalDateTime.now();
        System.out.println("Usuario inició sesión a las: " + inicioSesion);
    }

    public long parar(){
        LocalDateTime finSesion = LocalDateTime.now();
        System.out.println("Usuario cerró sesión a las: " + finSesion);

        LocalDateTime finDelDia = LocalDateTime.of(inicioSesion.toLocalDate(), LocalTime.MAX);

        LocalDateTime finConsiderado = finSesion.toLocalDate().equals(inicioSesion.toLocalDate()) ? finSesion : finDelDia;

        return Duration.between(inicioSesion, finConsiderado).toMinutes();
    }
}