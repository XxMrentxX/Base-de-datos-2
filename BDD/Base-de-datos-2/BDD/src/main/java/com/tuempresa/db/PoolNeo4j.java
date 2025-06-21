package com.tuempresa.db;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

public class PoolNeo4j {

    private static PoolNeo4j instancia;
    private static final String URI = "bolt://127.0.0.1:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "}\"9Y'ZdKPQYf7;6";
    private Driver driver;

    private PoolNeo4j() { driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));}

    public static PoolNeo4j getInstancia(){
        if(instancia == null)
            instancia = new PoolNeo4j();
        return instancia;
    }

    public Session getSession() { return driver.session();}
}