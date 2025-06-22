package com.tuempresa.auxiliares;

import com.tuempresa.clasesabstractas.Cambio;

import java.util.Date;

public class CambioCampo extends Cambio {
    private String campoModificado;
    private String valorAnterior;
    private String valorNuevo;

    public CambioCampo(Date fecha, String operador, String campoModificado, String valorAnterior, String valorNuevo) {
        super(fecha, operador);
        this.campoModificado = campoModificado;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
    }

    public String getCampoModificado() {
        return campoModificado;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public String getValorNuevo() {
        return valorNuevo;
    }

    @Override
    public String getTipo() {
        return "campo";
    }

    @Override
    public String getResumen() {
        return "Cambio en '" + campoModificado + "': " + valorAnterior + " → " + valorNuevo;
    }
}
