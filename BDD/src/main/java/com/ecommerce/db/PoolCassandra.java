package com.ecommerce.db;

import com.datastax.oss.driver.api.core.CqlSession;


public class PoolCassandra {
    public static void test() {
        try (CqlSession session = CqlSession.builder().build()) {
            System.out.println("Cassandra: Conexión exitosa");
        } catch (Exception e) {
            System.out.println("Cassandra: Error de conexión - " + e.getMessage());
        }
    }
}