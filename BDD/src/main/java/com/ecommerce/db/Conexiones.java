package com.ecommerce.db;

import redis.clients.jedis.Jedis;
import com.ecommerce.exceptions.ErrorConectionRedisException;

import org.neo4j.driver.Result;

import com.mongodb.client.MongoDatabase;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.internal.core.metadata.DefaultEndPoint;

import java.net.InetSocketAddress;

public class Conexiones {

    public void testRedis() {
        System.out.println("Accediendo a Redis...");
        try {
            Jedis jedis = PoolRedis.getInstancia().getConection();
            if (jedis != null) {
                jedis.set("clave", "valorDesdeConexiones");
                System.out.println("Valor en Redis: " + jedis.get("clave") + "\n");
            } else {
                System.out.println("No se pudo obtener conexión a Redis\n");
            }
        } catch (ErrorConectionRedisException e) {
            System.out.println("Error al conectar a Redis:");
            e.printStackTrace();
        }
    }

    public void testNeo4j() {
        System.out.println("Accediendo a Neo4j...");
        String query = "MATCH (n) RETURN COUNT(n) AS total";
        Result result = PoolNeo4j.getInstancia().getSession().run(query);
        if (result.hasNext()) {
            long total = result.next().get("total").asLong();
            System.out.println("Nodos en la base de datos Neo4j: " + total + "\n");
        } else {
            System.out.println("No se encontraron nodos en Neo4j\n");
        }
    }

    public void testCassandra() {
        System.out.println("Accediendo a Cassandra...");
        try (CqlSession session = CqlSession.builder()
                .addContactEndPoint(new DefaultEndPoint(new InetSocketAddress("0.0.0.0", 9042)))
                .withKeyspace("ecommerce")
                .withLocalDatacenter("datacenter1")
                .build()) {
            System.out.println("Conexión a Cassandra establecida correctamente\n");
        } catch (Exception e) {
            System.out.println("Error al conectar con Cassandra:");
            e.printStackTrace();
        }
    }

    public void testMongoDB() {
        System.out.println("Accediendo a MongoDB...");
        MongoDatabase db = PoolMongoDB.getInstancia().getConexion("miBaseDeDatos");

        if (db != null) {
            System.out.println("Conexión a MongoDB establecida correctamente");

            // Operación de prueba
//            MongoCollection<Document> coleccion = db.getCollection("miColeccion");
//            Document doc = new Document("campo", "valorDesdeConexiones");
//            coleccion.insertOne(doc);
//            Document resultado = coleccion.find().first();
//            if (resultado != null) {
//                System.out.println("Documento insertado: " + resultado.toJson() + "\n");
//            }
        } else {
            System.out.println("No se pudo conectar a MongoDB\n");
        }
    }
}