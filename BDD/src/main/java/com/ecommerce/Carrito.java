package com.ecommerce;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.ecommerce.db.PoolMongoDB;
import com.ecommerce.pedido.ItemPedido;
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
    private static Map<String, Integer> versionActualUsuario = new HashMap<>();


    public Carrito(String dni, String nombre, String apellido, String direccion, String condicionIVA) {
        MongoDatabase db = PoolMongoDB.getInstancia().getConexion("carritoDB");
        this.dniCliente = dni;
        this.nombreCliente = nombre;
        this.apellidoCliente = apellido;
        this.direccion = direccion;
        this.condicionIVA = condicionIVA;
        coleccion = db.getCollection("estados_carrito");
        versionActualUsuario.putIfAbsent(dni, 0);
    }

    public void agregarItem(String usuario, ItemPedido item) {
        List<ItemPedido> items = restaurarUltimoEstado(usuario);
        items.add(item);
        this.itemsPedido = items;
        guardarEstado(usuario, items);
    }

    public void eliminarItem(String usuario, UUID idProducto) {
        List<ItemPedido> items = restaurarUltimoEstado(usuario);
        items.removeIf(i -> i.getProductoId().equals(idProducto));
        this.itemsPedido = items;
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

        int nuevaVersion = obtenerProximaVersion(usuario);
        versionActualUsuario.put(usuario, nuevaVersion);

        Document doc = new Document()
                .append("usuario", usuario)
                .append("timestamp", Instant.now().toString())
                .append("items", itemsDoc)
                .append("version", nuevaVersion);

        coleccion.insertOne(doc);
    }

    public List<ItemPedido> restaurarUltimoEstado(String usuario) {
        int version = obtenerUltimaVersion(usuario);
        if (version == 0) {
            this.itemsPedido = new ArrayList<>();
            return this.itemsPedido;
        }

        List<ItemPedido> restaurado = restaurarEstadoPorVersion(usuario, version);
        this.itemsPedido = restaurado;
        return restaurado;
    }

    private int obtenerProximaVersion(String usuario) {
        Document ultimo = coleccion.find(new Document("usuario", usuario))
                .sort(new Document("version", -1))
                .first();
        return (ultimo != null) ? ultimo.getInteger("version") + 1 : 1;
    }

    private List<ItemPedido> restaurarEstadoPorVersion(String usuario, int version) {
        Document doc = coleccion.find(new Document("usuario", usuario).append("version", version)).first();
        List<ItemPedido> resultado = new ArrayList<>();

        if (doc != null && doc.containsKey("items")) {
            List<Document> docs = (List<Document>) doc.get("items");
            for (Document d : docs) {
                UUID id = UUID.fromString(d.getString("producto_id"));
                String nombre = d.getString("nombre");
                String empresa = d.getString("empresa");
                int cantidad = d.getInteger("cantidad");
                double precio = d.getDouble("precio_unitario");
                double subtotal = d.getDouble("subtotal");
                double iva = d.getDouble("iva");
                Double descuento = d.getDouble("porcentaje_descuento");
                if (descuento == null) {
                    descuento = 0.0;
                }
                double total = d.getDouble("total");

                ItemPedido item = new ItemPedido(id, nombre, cantidad, empresa, precio, subtotal, iva, descuento, total);
                this.itemsPedido = resultado;
                resultado.add(item);
            }
        }

        return resultado;
    }

    public boolean restaurarEstadoAnterior(String usuario) {
        int versionActual = versionActualUsuario.getOrDefault(usuario, 1);

        if (versionActual <= 1) {
            return false;
        }

        Document estadoAnterior = coleccion.find(new Document("usuario", usuario)
                .append("version", versionActual - 1)).first();

        if (estadoAnterior == null) return false;

        List<ItemPedido> items = obtenerItemsDesdeDocumento(estadoAnterior);
        this.itemsPedido = items;
        guardarEstado(usuario, items);
        versionActualUsuario.put(usuario, versionActual - 1);

        return true;
    }

    public boolean restaurarEstadoSiguiente(String usuario) {
        int versionActual = versionActualUsuario.getOrDefault(usuario, 1);
        int proximaVersion = versionActual + 1;

        Document estadoPosterior = coleccion.find(new Document("usuario", usuario)
                .append("version", proximaVersion)).first();

        if (estadoPosterior == null) {
            return false;
        }

        List<ItemPedido> items = obtenerItemsDesdeDocumento(estadoPosterior);
        this.itemsPedido = items;
        guardarEstado(usuario, items);
        versionActualUsuario.put(usuario, proximaVersion);

        return true;
    }

    private int obtenerUltimaVersion(String usuario) {
        Document ultimo = coleccion.find(new Document("usuario", usuario))
                .sort(new Document("version", -1))
                .first();

        return (ultimo != null) ? ultimo.getInteger("version") : 0;
    }

    private List<ItemPedido> obtenerItemsDesdeDocumento(Document doc) {
        List<ItemPedido> resultado = new ArrayList<>();
        List<Document> docs = (List<Document>) doc.get("items");

        if (docs != null) {
            for (Document d : docs) {
                UUID id = UUID.fromString(d.getString("producto_id"));
                String nombre = d.getString("nombre");
                String empresa = d.getString("empresa");
                int cantidad = d.getInteger("cantidad");
                double precio = d.getDouble("precio_unitario");
                double subtotal = d.getDouble("subtotal");
                double iva = d.getDouble("iva");
                Double descuento = d.getDouble("porcentaje_descuento");
                if (descuento == null) {
                    descuento = 0.0;
                }
                double total = d.getDouble("total");

                ItemPedido item = new ItemPedido(id, nombre, cantidad, empresa, precio, subtotal, iva, descuento, total);
                this.itemsPedido = resultado;
                resultado.add(item);
            }
        }

        return resultado;
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