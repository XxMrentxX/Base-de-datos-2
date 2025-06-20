package com.tuempresa.db;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class PoolMongoDB {

    private static PoolMongoDB instancia;
    private MongoClient cliente;

    private PoolMongoDB() {
        cliente = MongoClients.create("mongodb://localhost:27017");
    }

    public static PoolMongoDB getInstancia() {
        if (instancia == null) {
            instancia = new PoolMongoDB();
        }
        return instancia;
    }

    public MongoDatabase getConexion(String nombreBase) {
        try {
            MongoDatabase db = cliente.getDatabase(nombreBase);

            // Intentamos hacer ping para verificar conexión
            Document ping = new Document("ping", 1);
            db.runCommand(ping);

            return db;
        } catch (MongoException e) {
            System.err.println("❌ Error al conectarse con MongoDB: " + e.getMessage());
            return null;
        }
    }
}
