package com.tuempresa;

import com.datastax.oss.driver.api.core.CqlSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.tuempresa.db.PoolMongoDB;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.UUID;

public class Facturacion {

    public static void generarFactura(String idPedido, String formaPago, CqlSession session) {

        try {
            MongoDatabase dbMongo = PoolMongoDB.getInstancia().getConexion("pedidosDB");
            MongoCollection<Document> pedidos = dbMongo.getCollection("pedidos");

            // 1. Obtener pedido de Mongo (Supongo que antes de convertir al pedido hay que guardar los datos del carrito como pedido en MongoDB)
            Document pedido = pedidos.find(Filters.eq("_id", new ObjectId(idPedido))).first();

            if (pedido == null) {
                System.out.println("No se encontró el pedido con ese ID.");
                return;
            }

            guardarFacturaEnCassandra(pedido, formaPago, session);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al generar la factura.");
        }
    }

    private static void guardarFacturaEnCassandra(Document pedido, String formaPago, CqlSession session) {
        try {
            UUID idFactura = UUID.randomUUID();

            String idPedido = pedido.getObjectId("_id").toHexString();
            String dniCliente = pedido.getString("dniCliente");
            String nombreCliente = pedido.getString("nombreCliente");
            String direccion = pedido.getString("direccion");
            String fechaHora = java.time.LocalDateTime.now().toString();


            ObjectMapper mapper = new ObjectMapper();
            String itemsJSON = mapper.writeValueAsString(pedido.get("items"));

            String query = "INSERT INTO facturas (id_factura, id_pedido, dni_cliente, nombre_cliente, direccion, condicion_iva, forma_pago, fecha_hora, total_sin_iva, total_iva, total_con_iva, items) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

            session.execute(session.prepare(query).bind(
                    idFactura, idPedido, dniCliente, nombreCliente, direccion, formaPago,
                    fechaHora, itemsJSON
            ));

            System.out.println("Factura guardada en Cassandra con ID: " + idFactura);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al guardar la factura en Cassandra.");
        }
    }
}
