package com.tuempresa;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import com.mongodb.client.FindIterable;

public class Stock {

    private final MongoCollection<Document> coleccion;

    public Stock() {
        MongoDatabase db = com.tuempresa.db.PoolMongoDB.getInstancia().getConexion("carritoDB");
        this.coleccion = db.getCollection("productos");
    }

    public void listarProductosEnStock() {
        FindIterable<Document> productos = coleccion.find();

        System.out.println("Productos en stock:");
        for (Document producto : productos) {
            String nombre = producto.getString("nombre");
            Double precio = producto.getDouble("precio");
            Integer stock = producto.getInteger("stock");

            System.out.printf("- %s | Precio: %.2f | Stock: %d%n", nombre, precio, stock);
        }
    }

    public static void verificarBDD(List<Producto> listaProductos) {
        MongoDatabase db = com.tuempresa.db.PoolMongoDB.getInstancia().getConexion("carritoDB");

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
                    .append("precio_actual", p.getPrecio_actual())
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
