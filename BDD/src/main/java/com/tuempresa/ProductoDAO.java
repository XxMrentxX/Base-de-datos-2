package com.tuempresa;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {
    private final MongoCollection<Producto> coleccion;

    public ProductoDAO(MongoDatabase database) {
        this.coleccion = database.getCollection("productos", Producto.class);
    }

    // Crear producto
    public void insertarProducto(Producto producto) {
        coleccion.insertOne(producto);
    }

    // Buscar producto por ID
    public Producto buscarPorId(String id) {
        return coleccion.find(Filters.eq("_id", id)).first();
    }

    // Obtener todos los productos (devuelve lista)
    public List<Producto> obtenerProductos() {
        return coleccion.find().into(new ArrayList<>());
    }

    // Actualizar producto (reemplaza documento)
    public void actualizarProducto(Producto producto) {
        coleccion.replaceOne(Filters.eq("_id", producto.getId()), producto);
    }

    // Eliminar producto
    public void eliminarProducto(ObjectId id) {
        coleccion.deleteOne(Filters.eq("_id", id));
    }
}