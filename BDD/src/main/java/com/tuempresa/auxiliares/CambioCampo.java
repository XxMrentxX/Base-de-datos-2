package com.tuempresa.auxiliares;

import java.util.Date;

public class CambioCampo {
    private Date fecha;
    private String campo_modificado;
    private String valor_anterior;
    private String valor_nuevo;
    private String operador;

    // Getters y Setters
    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getCampo_modificado() {
        return campo_modificado;
    }

    public void setCampo_modificado(String campo_modificado) {
        this.campo_modificado = campo_modificado;
    }

    public String getValor_anterior() {
        return valor_anterior;
    }

    public void setValor_anterior(String valor_anterior) {
        this.valor_anterior = valor_anterior;
    }

    public String getValor_nuevo() {
        return valor_nuevo;
    }

    public void setValor_nuevo(String valor_nuevo) {
        this.valor_nuevo = valor_nuevo;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }
}
