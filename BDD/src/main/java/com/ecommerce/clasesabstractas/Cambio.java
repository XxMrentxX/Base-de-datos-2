package com.ecommerce.clasesabstractas;

import java.util.Date;

public abstract class Cambio {
    private Date fecha;
    private String operador;

    public Cambio(Date fecha, String operador) {
        this.fecha = fecha;
        this.operador = operador;
    }

    public Date getFecha() {
        return fecha;
    }

    public String getOperador() {
        return operador;
    }

    public abstract String getTipo();  // "precio" o "campo"

    public abstract String getResumen(); // info legible para mostrar o auditar
}
