package com.tuempresa.db;

import com.datastax.oss.driver.api.core.CqlSession;
import java.net.InetSocketAddress;


public class PoolCassandra {
    public static void test() {
        try (CqlSession session = CqlSession.builder().build()) {
            System.out.println("Cassandra: Conexión exitosa");
        } catch (Exception e) {
            System.out.println("Cassandra: Error de conexión - " + e.getMessage());
        }
    }
}