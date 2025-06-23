package com.ecommerce.auxiliares;

import com.ecommerce.clasesabstractas.Cambio;

import java.util.Date;

public class CambioPrecio extends Cambio {
    private double precioAnterior;
    private double precioNuevo;

    public CambioPrecio(Date fecha, String operador, double precioAnterior, double precioNuevo) {
        super(fecha, operador);
        this.precioAnterior = precioAnterior;
        this.precioNuevo = precioNuevo;
    }

    public double getPrecioAnterior() {
        return precioAnterior;
    }

    public double getPrecioNuevo() {
        return precioNuevo;
    }

    @Override
    public String getTipo() {
        return "precio";
    }

    @Override
    public String getResumen() {
        return "Cambio de precio: " + precioAnterior + " → " + precioNuevo;
    }
}
