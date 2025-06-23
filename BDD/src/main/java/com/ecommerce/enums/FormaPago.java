package com.ecommerce.enums;

public enum FormaPago {

    EFECTIVO("Efectivo"),
    TARJETA_DEBITO("Tarjeta de débito"),
    TARJETA_CREDITO("Tarjeta de crédito"),
    TRANSFERENCIA("Transferencia bancaria"),
    MERCADO_PAGO("Mercado Pago");

    private final String descripcion;

    FormaPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static FormaPago fromInput(String input) {
        try {
            int idx = Integer.parseInt(input);
            if (idx >= 1 && idx <= values().length) {
                return values()[idx - 1];
            }
        } catch (NumberFormatException ignore) { }

        for (FormaPago fp : values()) {
            if (fp.descripcion.equalsIgnoreCase(input)
                    || fp.name().equalsIgnoreCase(input)) {
                return fp;
            }
        }
        return null;      // no coincide
    }
}
