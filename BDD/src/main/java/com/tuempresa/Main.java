package com.tuempresa;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.tuempresa.db.PoolRedis;
import com.tuempresa.exceptions.ErrorConectionRedisException;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.tuempresa.auxiliares.Timer;


/*
* SESION --> REDIS
* GESTION DE PRODUCTOS --> MONGO Y REDIS
* 
* */

public class Main {

    public static void main(String[] args) {
        Conexiones conexiones = new Conexiones();

        conexiones.testRedis();
        conexiones.testNeo4j();
        conexiones.testCassandra();
        conexiones.testMongoDB();

        Timer timer = new Timer();

        try {
            Jedis jedis = PoolRedis.getInstancia().getConection();
            if (jedis != null) {

                Scanner scanner = new Scanner(System.in);

                System.out.println("1. Ingresar cuenta \n");
                System.out.println("2. Registrar cuenta nueva");
                String opcion = scanner.nextLine();

                if (opcion.equals("1")) {
                    System.out.println("Ingrese DNI");
                    String DNI = scanner.nextLine();
                    String claveRedis = "usuario:" + DNI;

                    if (!jedis.exists(claveRedis)) {
                        System.out.println("El usuario ingresado no existe");
                    } else {

                    }

                } else if (opcion.equals("2")) {
                    System.out.println("Ingrese DNI:");
                    String DNI = scanner.nextLine();

                    System.out.println("Ingrese contraseña");
                    String Contraseña = scanner.nextLine();

                    System.out.println("Ingrese Nombre:");
                    String Nombre = scanner.nextLine();

                    System.out.println("Ingrese mail:");
                    String Mail = scanner.nextLine();

                    Map<String, String> datosUsuario = new HashMap<>();
                    datosUsuario.put("contraseña", Contraseña);
                    datosUsuario.put("Nombre", Nombre);
                    datosUsuario.put("Mail", Mail);

                    jedis.hset("usuario:" + DNI, datosUsuario);

                    scanner.close();

                } else {
                    System.out.println("Error al ingresar datos.\n");
                }
            }
        } catch (ErrorConectionRedisException e) {
            throw new RuntimeException(e);
        }

        // Usuario se loguea, iniciar timer
        timer.iniciar();

        int opcion = 0;
        while(opcion != 6){
            Scanner scanner = new Scanner(System.in);

            System.out.println("1. Agregar Producto \n");
            System.out.println("2. Eliminar Producto \n");
            System.out.println("3. Cambiar Producto \n");
            System.out.println("4. Confirmar Pedido \n");
            System.out.println("5. Mostrar Catalogo \n");
            System.out.println("6. Mostrar Carrito \n");
            System.out.println("7. Terminar Sesion \n");
            opcion = scanner.nextInt();
            switch (opcion) {
                case 1:
                    System.out.println("Agregar Producto");
                case 7:
                    System.out.println("Terminando el programa...");
                    timer.parar();
                    break;
            }

        }


        // PARTE 3 - GESTION DE PRODUCTOS
        /*
        ============== EJEMPLO DE IMPLEMENTACIÓN DEL MAIN DEL CATÁLOGO, NO ES EL CARRITO
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = client.getDatabase("tienda");

        ProductoDAO dao = new ProductoDAO(db);

        Producto p = new Producto();
        p.setNombre("Mouse Gamer");
        p.setDescripcion("Con luces RGB y sensor de alta precisión");
        p.setPrecio_actual(5999.99);

        dao.insertarProducto(p);  // lo guarda en Mongo

         */
    }
}
