package com.tuempresa;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.tuempresa.db.PoolMongoDB;
import org.bson.Document;
import java.time.Instant;
import java.util.*;

// PARTE 3 - GESTIONAR PRODUCTOS EN EL CARRITO
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

    // VERIFICAR SI FALTA TRAER DATOS DE PRODUCTOS Y AGREGARLOS SI FALTA
    public static void agregarProducto(String usuario, Producto producto) {
        List<Producto> productos = restaurarUltimoEstado(usuario);
        productos.add(producto);
        guardarEstado(usuario, productos);
    }

    public static void eliminarProducto(String usuario, String nombreProducto) {
        List<Producto> productos = restaurarUltimoEstado(usuario);
        productos.removeIf(p -> p.getNombre().equalsIgnoreCase(nombreProducto));
        guardarEstado(usuario, productos);
    }


    // PARTE 4 - GUARDAR Y RECUPERAR ESTADOS DEL CARRITO
    public static void guardarEstado(String usuario, List<Producto> productos) {
        List<Document> productosDoc = new ArrayList<>();

        for (Producto p : productos) {
            Document prodDoc = new Document()
                    .append("id", p.getId())
                    .append("nombre", p.getNombre())
                    .append("descripcion", p.getDescripcion())
                    .append("precio_actual", p.getPrecio_actual());
            productosDoc.add(prodDoc);
        }

        Document doc = new Document()
                .append("usuario", usuario)
                .append("timestamp", Instant.now().toString())
                .append("productos", productosDoc)
                .append("version", obtenerProximaVersion(usuario));

        coleccion.insertOne(doc);
    }

    public static List<Producto> restaurarUltimoEstado(String usuario) {
        Document ultimo = coleccion.find(new Document("usuario", usuario))
                .sort(new Document("version", -1))
                .first();
        List<Producto> resultado = new ArrayList<>();

        if (ultimo != null && ultimo.containsKey("productos")) {
            List<Document> docs = (List<Document>) ultimo.get("productos");
            for (Document d : docs) {
                String id = d.getString("id");
                String nombre = d.getString("nombre");
                String descripcion = d.getString("descripcion");
                double precio = d.getDouble("precio_actual");

                // El resto de los campos los dejamos vacíos por ahora
                Producto p = new Producto(id, nombre, descripcion,
                        new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                        precio, new ArrayList<>(), new ArrayList<>());

                resultado.add(p);
            }
        }
        return resultado;
    }

    private static int obtenerProximaVersion(String usuario) {
        Document ultimo = coleccion.find(new Document("usuario", usuario))
                .sort(new Document("version", -1))
                .first();
        return (ultimo != null) ? ultimo.getInteger("version") + 1 : 1;
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
