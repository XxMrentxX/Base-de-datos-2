package com.ecommerce.pedido;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.ecommerce.Carrito;
import com.ecommerce.db.PoolMongoDB;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

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
                double iva = subtotalConDescuento * (carrito.getCondicionIVA().equals("Responsable Inscripto") ? 0.21 : 0);

                Document itemDoc = new Document()
                        .append("productoId", item.getProductoId().toString())
                        .append("nombre", item.getNombreProducto())
                        .append("cantidad", item.getCantidad())
                        .append("empresa", item.getEmpresa())
                        .append("precioUnitario", item.getPrecioUnitario())
                        .append("porcentajeDescuento", item.getPorcentajeDescuento())
                        .append("subtotal", subtotal)
                        .append("descuento", descuento)
                        .append("subtotalConDescuento", subtotalConDescuento)
                        .append("iva", iva);

                itemsDocuments.add(itemDoc);
                totalSinIVA += subtotalConDescuento;
                totalIVA += iva;
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

