package com.tuempresa;

import redis.clients.jedis.Jedis;
import com.tuempresa.db.PoolRedis;
import com.tuempresa.exceptions.ErrorConectionRedisException;

import com.tuempresa.db.PoolNeo4j;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.tuempresa.db.PoolMongoDB;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.internal.core.metadata.DefaultEndPoint;

import java.net.InetSocketAddress;

public class Funciones {
    public static void main(String[] args) {
        try {
            Jedis jedis = PoolRedis.getInstancia().getConection();
            if (jedis != null) {
                jedis.set("usuario:1:nombre", "Juan");
                jedis.set("usuario:1:email", "juan@email.com");
                System.out.println("Nombre desde Redis: " + jedis.get("usuario:1:nombre") + "\n");
            } else {
                System.out.println("No se pudo conectar a Redis.\n");
            }
        } catch (ErrorConectionRedisException e) {
            e.printStackTrace();
        }
    }

}
