package com.tuempresa.enums;

public enum Categoria {
    TOP,
    MEDIUM,
    LOW,
    UNDEFINED;

    private final String valor;

    Categoria() {
        this.valor = this.name().toLowerCase();
    }

    public String getValor() {
        return valor;
    }

    public static Categoria fromString(String texto) {
        for (Categoria categoria : Categoria.values()) {
            if (categoria.valor.equalsIgnoreCase(texto)) {
                return categoria;
            }
        }
        return UNDEFINED;
    }

}
