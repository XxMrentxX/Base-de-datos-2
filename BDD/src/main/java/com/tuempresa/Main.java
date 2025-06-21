package com.tuempresa;

import com.tuempresa.db.PoolRedis;
import com.tuempresa.exceptions.ErrorConectionRedisException;
import com.tuempresa.Carrito;
import redis.clients.jedis.Jedis;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


/*
 * SESION --> REDIS
 * GESTION DE PRODUCTOS --> MONGO Y REDIS
 *
 * */

public class Main {

    static {
        // Silenciar MongoDB driver (usa java.util.logging)
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);
    }

    public static void main(String[] args) {
        Conexiones conexiones = new Conexiones();

        conexiones.testRedis();
        conexiones.testNeo4j();
        conexiones.testCassandra();
        conexiones.testMongoDB();

        Timer timer = new Timer();
        boolean valor = menuInicio();
        if (valor){
            menuCarrito();
        }
    }

    public static boolean menuInicio() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1. Ingresar cuenta");
        System.out.println("2. Registrar cuenta nueva");
        System.out.println("3. Salir");
        String Opcion = scanner.nextLine();
        switch (Opcion) {
            case "1":
                Usuario.Login();
                break;
            case "2":
                Usuario.Registrarse();
                break;
            case "3":
                return false;
        }
        return false;
    }

    public static void menuCarrito() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1. Agregar producto");
        System.out.println("2. Eliminar producto");
        System.out.println("3. Cambiar productos");
        System.out.println("4. Restablecer cambio");
        String opcion = scanner.nextLine();

        switch (opcion){
            case "1":

            case "2":

            case "3":

            case "4":

        }
    }


}