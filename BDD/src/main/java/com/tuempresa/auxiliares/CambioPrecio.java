package com.tuempresa.auxiliares;

import java.util.Date;

public class CambioPrecio {
    private Date fecha;
    private double precio_anterior;
    private double precio_nuevo;
    private String operador;

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public double getPrecio_anterior() {
        return precio_anterior;
    }

    public void setPrecio_anterior(double precio_anterior) {
        this.precio_anterior = precio_anterior;
    }

    public double getPrecio_nuevo() {
        return precio_nuevo;
    }

    public void setPrecio_nuevo(double precio_nuevo) {
        this.precio_nuevo = precio_nuevo;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }
}
