package com.tuempresa;
import com.datastax.oss.driver.api.core.CqlSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.tuempresa.db.PoolMongoDB;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

// PARTE 6 - GENERAR FACTURA
public class Facturacion {

    public static void generarFactura(String idPedido, String formaPago, CqlSession session) {
        try {
            MongoDatabase dbMongo = PoolMongoDB.getInstancia().getConexion("pedidosDB");
            MongoCollection<Document> pedidos = dbMongo.getCollection("pedidos");

            Document pedido = pedidos.find(Filters.eq("_id", new ObjectId(idPedido))).first();

            if (pedido == null) {
                System.out.println("No se encontró el pedido con ese ID.");
                return;
            }

            List<Document> items = (List<Document>) pedido.get("items");
            Map<String, List<Document>> itemsPorEmpresa = new HashMap<>();

            for (Document item : items) {
                String empresa = item.getString("empresa");
                itemsPorEmpresa.computeIfAbsent(empresa, k -> new ArrayList<>()).add(item);
            }

            for (Map.Entry<String, List<Document>> entry : itemsPorEmpresa.entrySet()) {
                String empresa = entry.getKey();
                List<Document> itemsEmpresa = entry.getValue();

                Map<String, Object> resultado = guardarFacturaPorEmpresa(pedido, itemsEmpresa, formaPago, session);
                UUID idFactura = (UUID) resultado.get("idFactura");
                String dniCliente = (String) resultado.get("dniCliente");

                ControladorOps.registrarOperacionFacturacion(
                        session, idFactura, idPedido, dniCliente, empresa, formaPago
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al generar la factura.");
        }
    }

    private static Map<String, Object> guardarFacturaPorEmpresa(Document pedido, List<Document> items, String formaPago, CqlSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            UUID idFactura = UUID.randomUUID();

            String idPedido = pedido.getObjectId("_id").toHexString();
            String dniCliente = pedido.getString("dniCliente");
            String nombreCliente = pedido.getString("nombreCliente");
            String direccion = pedido.getString("direccion");
            String condicionIVA = pedido.getString("condicionIVA");
            String fechaHora = java.time.LocalDateTime.now().toString();

            double totalSinIVA = 0;
            double totalIVA = 0;
            double totalConIVA = 0;

            for (Document item : items) {
                double subtotal = item.getDouble("subtotal");
                double iva = item.getDouble("iva");
                totalSinIVA += subtotal;
                totalIVA += iva;
                totalConIVA += subtotal + iva;
            }

            ObjectMapper mapper = new ObjectMapper();
            String itemsJSON = mapper.writeValueAsString(items);

            String query = "INSERT INTO facturas (id_factura, id_pedido, dni_cliente, nombre_cliente, direccion, condicion_iva, forma_pago, fecha_hora, total_sin_iva, total_iva, total_con_iva, items) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

            session.execute(session.prepare(query).bind(
                    idFactura, idPedido, dniCliente, nombreCliente, direccion, condicionIVA,
                    formaPago, fechaHora, totalSinIVA, totalIVA, totalConIVA, itemsJSON
            ));

            System.out.println("Factura generada para empresa '" + items.get(0).getString("empresa") + "' con ID: " + idFactura);

            result.put("idFactura", idFactura);
            result.put("dniCliente", dniCliente);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al guardar la factura en Cassandra.");
        }
        return result;
    }
}
