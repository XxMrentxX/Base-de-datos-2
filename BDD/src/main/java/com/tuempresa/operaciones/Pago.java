package com.tuempresa.operaciones;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.time.LocalDateTime;
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

    public static void mostrarPagos(String dniCliente, CqlSession session) {
        String query = "SELECT id_pago, id_factura, forma_pago, monto, fecha_hora FROM pagos WHERE dni_cliente = ? ALLOW FILTERING;";
        ResultSet resultado = session.execute(session.prepare(query).bind(dniCliente));


        if (resultado.all().isEmpty()) {
            System.out.println("No se encontraron pagos para este cliente.");
            return;
        }

        System.out.println("\nPagos del cliente: " + dniCliente);

        for (Row row : resultado) {
            System.out.printf("\n- ID Pago: %s\n  Factura: %s\n  Forma: %s\n  Monto: $%.2f\n  Fecha: %s\n",
                    row.getUuid("id_pago"),
                    row.getUuid("id_factura"),
                    row.getString("forma_pago"),
                    row.getDouble("monto"),
                    row.getString("fecha_hora")
            );
        }
    }
}
