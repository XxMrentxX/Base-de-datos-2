package com.tuempresa;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.tuempresa.db.PoolMongoDB;
import com.tuempresa.pedido.ItemPedido;
import org.bson.Document;

import java.time.Instant;
import java.util.*;

public class Carrito {
    private String dniCliente;
    private String nombreCliente;
    private String apellidoCliente;
    private String direccion;
    private String condicionIVA;
    private List<ItemPedido> itemsPedido = new ArrayList<>();

    private static MongoCollection<Document> coleccion = null;

    public Carrito(String dni, String nombre, String apellido, String direccion, String condicionIVA) {
        MongoDatabase db = PoolMongoDB.getInstancia().getConexion("carritoDB");
        this.dniCliente = dni;
        this.nombreCliente = nombre;
        this.apellidoCliente = apellido;
        this.direccion = direccion;
        this.condicionIVA = condicionIVA;
        coleccion = db.getCollection("estados_carrito");
    }

    public void agregarItem(String usuario, ItemPedido item) {
        List<ItemPedido> items = restaurarUltimoEstado(usuario);
        items.add(item);
        guardarEstado(usuario, items);
    }

    public void eliminarItem(String usuario, UUID idProducto) {
        List<ItemPedido> items = restaurarUltimoEstado(usuario);
        items.removeIf(i -> i.getProductoId().equals(idProducto));
        guardarEstado(usuario, items);
    }

    public void guardarEstado(String usuario, List<ItemPedido> items) {
        List<Document> itemsDoc = new ArrayList<>();

        for (ItemPedido item : items) {
            Document docItem = new Document()
                    .append("producto_id", item.getProductoId().toString())
                    .append("nombre", item.getNombreProducto())
                    .append("empresa", item.getEmpresa())
                    .append("cantidad", item.getCantidad())
                    .append("precio_unitario", item.getPrecioUnitario())
                    .append("subtotal", item.getSubtotal())
                    .append("iva", item.getIva())
                    .append("porcentaje_descuento", item.getPorcentajeDescuento())
                    .append("total", item.getTotal());
            itemsDoc.add(docItem);
        }

        Document doc = new Document()
                .append("usuario", usuario)
                .append("timestamp", Instant.now().toString())
                .append("items", itemsDoc)
                .append("version", obtenerProximaVersion(usuario));

        coleccion.insertOne(doc);
    }

    public List<ItemPedido> restaurarUltimoEstado(String usuario) {
        Document ultimo = coleccion.find(new Document("usuario", usuario))
                .sort(new Document("version", -1))
                .first();

        List<ItemPedido> resultado = new ArrayList<>();

        if (ultimo != null && ultimo.containsKey("items")) {
            List<Document> docs = (List<Document>) ultimo.get("items");
            for (Document d : docs) {
                UUID id = UUID.fromString(d.getString("producto_id"));
                String nombre = d.getString("nombre");
                String empresa = d.getString("empresa");
                int cantidad = d.getInteger("cantidad");
                double precio = d.getDouble("precio_unitario");
                double subtotal = d.getDouble("subtotal");
                double iva = d.getDouble("iva");
                double descuento = d.getDouble("porcentaje_descuento");
                double total = d.getDouble("total");

                ItemPedido item = new ItemPedido(nombre, cantidad, empresa, precio, subtotal, iva, descuento, total);
                resultado.add(item);
            }
        }
        return resultado;
    }

    private int obtenerProximaVersion(String usuario) {
        Document ultimo = coleccion.find(new Document("usuario", usuario))
                .sort(new Document("version", -1))
                .first();
        return (ultimo != null) ? ultimo.getInteger("version") + 1 : 1;
    }

    public void imprimirCarrito() {
        if (itemsPedido.isEmpty()) {
            System.out.println("El carrito está vacío.");
            return;
        }

        System.out.println("\nCarrito de compras:");

        int i = 1;
        for (ItemPedido item : itemsPedido) {
            System.out.println("Ítem #" + i++);
            System.out.println("Producto: " + item.getNombreProducto());
            System.out.println("Empresa: " + item.getEmpresa());
            System.out.println("Cantidad: " + item.getCantidad());
            System.out.println("Precio unitario: $" + item.getPrecioUnitario());
            System.out.println("Subtotal: $" + item.getSubtotal());
            System.out.println("IVA: $" + item.getIva());
            System.out.println("Descuento: " + item.getPorcentajeDescuento() + "%");
            System.out.println("Total: $" + item.getTotal());
            System.out.println("-------------------------------------------------------------");
        }

        double totalFinal = itemsPedido.stream().mapToDouble(ItemPedido::getTotal).sum();
        System.out.printf("Total general del carrito: $%.2f\n", totalFinal);
    }

    public List<ItemPedido> getItemsPedido() {
        return itemsPedido;
    }

    public String getDniCliente() {
        return dniCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public String getApellidoCliente() {
        return apellidoCliente;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getCondicionIVA() {
        return condicionIVA;
    }

    public void agregarItemPedido(ItemPedido item) {
        this.itemsPedido.add(item);
    }
}
