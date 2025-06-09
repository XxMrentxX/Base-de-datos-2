package com.tuempresa;

import com.tuempresa.db.PoolRedis;
import com.tuempresa.exceptions.ErrorConectionRedisException;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import java.util.Scanner;

import com.tuempresa.Timer;


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
                String Opcion = scanner.nextLine();

                if (Opcion.equals("1")) {
                    System.out.println("Ingrese DNI");
                    String DNI = scanner.nextLine();
                    String claveRedis = "usuario:" + DNI;

                    if (!jedis.exists(claveRedis)) {
                        System.out.println("El usuario ingresado no existe");
                    } else {

                    }

                } else if (Opcion.equals("2")) {
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

        // Se loguea, entonces iniciamos timer
        timer.iniciar();

        // PARTE 3 - GESTION DE PRODUCTOS

    }
}
