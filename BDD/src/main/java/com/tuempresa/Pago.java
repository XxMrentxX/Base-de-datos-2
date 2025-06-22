package com.tuempresa;

import com.datastax.oss.driver.api.core.CqlSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// PARTE 6 - REGISTRAR PAGO CON METODO
public class Pago {
    private static Pago instancia;

    private Pago() {}

    public static Pago getInstancia() {
        if (instancia == null) {
            instancia = new Pago();
        }
        return instancia;
    }

    public static UUID registrarPago(CqlSession session, Map<UUID, Double> facturasConMonto, String dni, String formaPago, String operador, String estado) {

        UUID idPago = UUID.randomUUID();
        String fechaHora = LocalDateTime.now().toString();

        String query = "INSERT INTO pagos (id_pago, id_factura, dni_cliente, forma_pago, monto, fecha_hora) "
                + "VALUES (?, ?, ?, ?, ?, ?);";

        // PARTE 8 - UN PAGO CUBRE VARIAS FACTURAS
        for (Map.Entry<UUID, Double> entry : facturasConMonto.entrySet()) {
            UUID idFactura = entry.getKey();
            double monto = entry.getValue();

            session.execute(session.prepare(query).bind(
                    idPago, idFactura, dni, formaPago, monto, fechaHora
            ));
        }

        ControladorOps.registrarOperacionPago(session, idPago, formaPago, operador, estado);

        System.out.println("Pago registrado con ID: " + idPago);
        return idPago;
    }
}
