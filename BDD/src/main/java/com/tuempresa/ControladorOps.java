package com.tuempresa;

import com.datastax.oss.driver.api.core.CqlSession;

import java.time.LocalDateTime;
import java.util.UUID;

// PARTE 7 - GUARDAR OPERACIONES DE FACTURACION/PAGOS
public class ControladorOps {
    private static ControladorOps instancia;

    private ControladorOps() {}

    public static ControladorOps getInstancia() {
        if (instancia == null) {
            instancia = new ControladorOps();
        }
        return instancia;
    }

    // Registrar detalle de operación de pago
    public static void registrarOperacionPago(CqlSession session, UUID idPago, String medio, String operador, String estado) {

        UUID idOperacion = UUID.randomUUID();

        String fechaHora = LocalDateTime.now().toString();

        String query = "INSERT INTO operaciones_pago (id_operacion, id_pago, medio, operador, fecha_hora, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?);";

        session.execute(session.prepare(query).bind(
                idOperacion, idPago, medio, operador, fechaHora, estado
        ));

        System.out.println("Operación registrada con ID: " + idOperacion);
    }

    // Registrar detalles de facturación
    public static void registrarOperacionFacturacion(CqlSession session, UUID idFactura, String idPedido, String dni, String operador, String medio) {
        UUID idOperacion = UUID.randomUUID();
        String fechaHora = LocalDateTime.now().toString();

        String query = "INSERT INTO operaciones_facturacion (id_operacion, id_factura, id_pedido, dni_cliente, operador, medio, fecha_hora) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?);";

        session.execute(session.prepare(query).bind(
                idOperacion, idFactura, idPedido, dni, operador, medio, fechaHora
        ));

        System.out.println("🧾 Operación de facturación registrada con ID: " + idOperacion);
    }
}
