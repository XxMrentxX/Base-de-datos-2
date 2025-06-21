package com.tuempresa;

import com.datastax.oss.driver.api.core.CqlSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.tuempresa.db.PoolMongoDB;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;
import java.util.Date;
import com.tuempresa.ItemPedido;

// PARTE 6 - GENERAR FACTURA
public class PedidoService {
    private static PedidoService instancia;

    private PedidoService() {
    }

    public static PedidoService getInstancia(){
        if (instancia == null){
            instancia = new PedidoService();
        }
        return instancia;
    }

    // PARTE 5 - CONVERTIR CARRITO A PEDIDO
    public String guardarPedido(Carrito carrito) {
        try {
            MongoDatabase dbMongo = PoolMongoDB.getInstancia().getConexion("pedidosDB");
            MongoCollection<Document> pedidos = dbMongo.getCollection("pedidos");

            double totalSinIVA = 0;
            double totalIVA = 0;
            double totalConIVA = 0;

            List<Document> itemsDocuments = new ArrayList<>();
            List<ItemPedido> items = carrito.getItemsPedido();

            for (ItemPedido item : items) {
                double subtotal = item.getPrecioUnitario() * item.getCantidad();
                double descuento = subtotal * (item.getPorcentajeDescuento() / 100.0);
                double subtotalConDescuento = subtotal - descuento;

                Document itemDoc = new Document()
                        .append("productoId", item.getProductoId())
                        .append("nombre", item.getNombreProducto())
                        .append("cantidad", item.getCantidad())
                        .append("precioUnitario", item.getPrecioUnitario())
                        .append("porcentajeDescuento", item.getPorcentajeDescuento())
                        .append("subtotal", subtotal)
                        .append("descuento", descuento)
                        .append("subtotalConDescuento", subtotalConDescuento);

                itemsDocuments.add(itemDoc);
                totalSinIVA += subtotalConDescuento;
            }

            double porcentajeIVA = carrito.getCondicionIVA().equals("Responsable Inscripto") ? 0.21 : 0;
            totalIVA = totalSinIVA * porcentajeIVA;
            totalConIVA = totalSinIVA + totalIVA;

            Document pedidoDoc = new Document()
                    .append("fecha", new Date())
                    .append("dniCliente", carrito.getDniCliente())
                    .append("nombreCliente", carrito.getNombreCliente())
                    .append("apellidoCliente", carrito.getApellidoCliente())
                    .append("direccion", carrito.getDireccion())
                    .append("condicionIVA", carrito.getCondicionIVA())
                    .append("items", itemsDocuments)
                    .append("totalSinIVA", totalSinIVA)
                    .append("totalIVA", totalIVA)
                    .append("totalConIVA", totalConIVA);

            pedidos.insertOne(pedidoDoc);
            return pedidoDoc.getObjectId("_id").toString();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al guardar el pedido en MongoDB.");
            throw new RuntimeException("Error al guardar el pedido", e);
        }
    }
}

