package com.tuempresa.enums;

public enum CondicionIVA {
    RESPONSABLE_INSCRIPTO("Responsable Inscripto"),
    MONOTRIBUTISTA("Monotributista"),
    EXENTO("Exento"),
    NO_RESPONSABLE("No Responsable"),
    CONSUMIDOR_FINAL("Consumidor Final"),
    UNDEFINED("undefined");

    private final String valor;

    CondicionIVA(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static CondicionIVA fromString(String str) {
        for (CondicionIVA c : values()) {
            if (c.valor.equalsIgnoreCase(str) || c.name().equalsIgnoreCase(str)) {
                return c;
            }
        }
        return UNDEFINED;
    }
}
