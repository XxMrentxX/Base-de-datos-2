package com.ecommerce;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import com.ecommerce.producto.Producto;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.client.FindIterable;
import static com.mongodb.client.model.Filters.*;


public class Stock {

    private final MongoCollection<Document> coleccion;

    public Stock() {
        MongoDatabase db = com.ecommerce.db.PoolMongoDB.getInstancia().getConexion("carritoDB");
        this.coleccion = db.getCollection("productos");
    }

    public void listarProductosEnStock() {
        FindIterable<Document> productos = coleccion.find();

        System.out.println("\n=== CATÁLOGO DE PRODUCTOS ===");
        for (Document producto : productos) {
            mostrarProducto(producto);
        }
    }

    private void mostrarProducto(Document producto) {
        System.out.println("\n----------------------------------------");
        System.out.println("ID: " + producto.getString("id"));
        System.out.println("Nombre: " + producto.getString("nombre"));
        System.out.println("Descripción: " + producto.getString("descripcion"));
        System.out.printf("Precio: $%.2f%n", producto.getDouble("precio_actual"));
        System.out.println("Stock disponible: " + producto.getInteger("stock"));
        System.out.println("Descuento: " + producto.getDouble("descuento"));

        List<String> imagenes = producto.getList("imagenes", String.class);
        if (imagenes != null && !imagenes.isEmpty()) {
            System.out.println("Imágenes disponibles:");
            for (String img : imagenes) {
                System.out.println(" - " + img);
            }
        } else {
            System.out.println("No hay imágenes disponibles.");
        }

        List<String> videos = producto.getList("videos", String.class);
        if (videos != null && !videos.isEmpty()) {
            System.out.println("Videos disponibles:");
            for (String vid : videos) {
                System.out.println(" - " + vid);
            }
        } else {
            System.out.println("No hay videos disponibles.");
        }

        List<Document> comentarios = producto.getList("comentarios", Document.class);
        if (comentarios != null && !comentarios.isEmpty()) {
            System.out.println("Últimos comentarios:");
            comentarios.stream().limit(3).forEach(c ->
                    System.out.println("- " + c.getString("texto")));
        } else {
            System.out.println("Sin comentarios aún.");
        }

        System.out.println("----------------------------------------");
    }

    public Document buscarProducto(String id) {
        return coleccion.find(eq("id", id)).first();
    }

    public void actualizarPrecio(String id, double nuevoPrecio, String operador) {
        Document producto = buscarProducto(id);
        if (producto == null) {
            System.out.println("Producto no encontrado");
            return;
        }

        double precioAnterior = producto.getDouble("precio_actual");

        Document cambio = new Document()
                .append("fecha", LocalDateTime.now().toString())
                .append("tipo", "precio")
                .append("valor_anterior", precioAnterior)
                .append("valor_nuevo", nuevoPrecio)
                .append("operador", operador);

        Bson updates = Updates.combine(
                Updates.set("precio_actual", nuevoPrecio),
                Updates.push("historial_precio", cambio),
                Updates.push("historial_cambios", cambio)
        );

        coleccion.updateOne(eq("id", id), updates);

        registrarCambio(id, "precio", String.valueOf(precioAnterior), String.valueOf(nuevoPrecio), operador);
    }

    public void registrarCambio(String id, String tipo, String valorAnterior,
                                String valorNuevo, String operador) {
        Document cambio = new Document()
                .append("fecha", LocalDateTime.now().toString())
                .append("tipo", tipo)
                .append("valor_anterior", valorAnterior)
                .append("valor_nuevo", valorNuevo)
                .append("operador", operador);

        coleccion.updateOne(
                eq("id", id),
                Updates.push("historial_cambios", cambio)
        );
    }

    public void agregarComentario(String id, String texto, String usuario) {
        Document comentario = new Document()
                .append("fecha", LocalDateTime.now().toString())
                .append("texto", texto)
                .append("usuario", usuario);

        coleccion.updateOne(
                eq("id", id),
                Updates.push("comentarios", comentario)
        );
    }

    public void agregarProducto(Producto producto, int cantidadInicial, double descuento) {
        Document doc = new Document("id", producto.getId())
                .append("nombre", producto.getNombre())
                .append("descripcion", producto.getDescripcion())
                .append("empresa", producto.getEmpresa())
                .append("precio_actual", producto.getPrecioActual())
                .append("stock", cantidadInicial)
                .append("descuento", descuento);

        coleccion.insertOne(doc);
        System.out.println("Producto agregado con éxito.");
    }

    public void eliminarProductoPorId(String id) {
        coleccion.deleteOne(eq("id", id));
    }

    public void modificarCantidadStock(String id, int nuevaCantidad, String operador) {
        Document producto = buscarProducto(id);
        if (producto == null) {
            System.out.println("Producto no encontrado");
            return;
        }
        int stockAnterior = producto.getInteger("stock");

        Document cambio = new Document()
                .append("fecha", LocalDateTime.now().toString())
                .append("tipo", "stock")
                .append("valor_anterior", stockAnterior)
                .append("valor_nuevo", nuevaCantidad)
                .append("operador", operador);

        Bson updates = Updates.combine(
                Updates.set("stock", nuevaCantidad),
                Updates.push("historial_stock", cambio),
                Updates.push("historial_cambios", cambio)
        );

        coleccion.updateOne(eq("id", id), updates);
        System.out.println("Cantidad de stock actualizada.");
    }

    public static void verificarBDD(List<Producto> listaProductos) {
        MongoDatabase db = com.ecommerce.db.PoolMongoDB.getInstancia().getConexion("carritoDB");

        boolean existe = false;
        try (MongoCursor<String> nombresColecciones = db.listCollectionNames().iterator()) {
            while (nombresColecciones.hasNext()) {
                if (nombresColecciones.next().equals("productos")) {
                    existe = true;
                    break;
                }
            }
        }

        if (existe) {
            return;
        }

        MongoCollection<Document> productosCol = db.getCollection("productos");

        List<Document> docs = new ArrayList<>();
        for (Producto p : listaProductos) {
            Document prodDoc = new Document()
                    .append("id", p.getId())
                    .append("nombre", p.getNombre())
                    .append("descripcion", p.getDescripcion())
                    .append("precio_actual", p.getPrecioActual())
                    .append("imagenes", p.getImagenes())
                    .append("videos", p.getVideos())
                    .append("comentarios", new ArrayList<>())
                    .append("historial_precio", new ArrayList<>())
                    .append("historial_cambios", new ArrayList<>());

            docs.add(prodDoc);
        }

        productosCol.insertMany(docs);
    }
}
