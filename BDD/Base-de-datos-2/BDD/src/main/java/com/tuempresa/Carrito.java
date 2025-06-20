package com.tuempresa;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.time.Instant;
import java.util.*;

// PARTE 3 - GESTIONAR PRODUCTOS EN EL CARRITO
public class Carrito {

    private final MongoCollection<Document> coleccion;

    public Carrito() {
        MongoDatabase db = com.tuempresa.db.PoolMongoDB.getInstancia().getConexion("carritoDB");
        this.coleccion = db.getCollection("estados_carrito");
    }

    // PARTE 3 - GESTIONAR PRODUCTOS EN CARRITO
    // VERIFICAR SI FALTA TRAER DATOS DE PRODUCTOS Y AGREGARLOS SI FALTA
    public void agregarProducto(String usuario, String producto) {
        List<String> productos = restaurarUltimoEstado(usuario);
        productos.add(producto);
        guardarEstado(usuario, productos);
    }

    public void eliminarProducto(String usuario, String producto) {
        List<String> productos = restaurarUltimoEstado(usuario);
        productos.remove(producto); // elimina solo la primera ocurrencia
        guardarEstado(usuario, productos);
    }

    // PARTE 4 - GUARDAR Y RECUPERAR ESTADOS DEL CARRITO
    public void guardarEstado(String usuario, List<String> productos) {
        Document doc = new Document();
        doc.append("usuario", usuario);
        doc.append("timestamp", Instant.now().toString());
        doc.append("productos", productos);
        doc.append("version", obtenerProximaVersion(usuario));
        coleccion.insertOne(doc);
    }

    public List<String> restaurarUltimoEstado(String usuario) {
        Document ultimo = coleccion.find(new Document("usuario", usuario))
                .sort(new Document("version", -1))
                .first();
        if (ultimo != null) {
            return (List<String>) ultimo.get("productos");
        }
        return new ArrayList<>();
    }

    private int obtenerProximaVersion(String usuario) {
        Document ultimo = coleccion.find(new Document("usuario", usuario))
                .sort(new Document("version", -1))
                .first();
        return (ultimo != null) ? ultimo.getInteger("version") + 1 : 1;
    }


}
