package com.tuempresa.auxiliares;

import java.time.LocalDateTime;
import java.time.Duration;

// PARTE 2 - REGISTRAR TIEMPO DE ACTIVIDAD (CASSANDRA)
public class Timer {
    private String userID;
    private LocalDateTime inicioSesion;
    private LocalDateTime finSesion;
    private long tiempoConectado; // en minutos

    public void iniciar() {
        inicioSesion = LocalDateTime.now();
        System.out.println("Usuario inició sesión a las: " + inicioSesion);
    }

    public void parar(){
        inicioSesion = LocalDateTime.now();
        System.out.println("Usuario cerró sesión a las: " + inicioSesion);

        Duration duracion = Duration.between(inicioSesion, finSesion);
        tiempoConectado = duracion.toMinutes();
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public long getTiempoConectado() {
        return tiempoConectado;
    }
}
