package com.tuempresa;

import com.tuempresa.db.PoolRedis;
import com.tuempresa.exceptions.ErrorConectionRedisException;
import redis.clients.jedis.Jedis;

import java.util.*;

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
        menu();


//
//
//                } else if (Opcion.equals("2")) {
//
//            }
//        } catch (ErrorConectionRedisException e) {
//            throw new RuntimeException(e);
//        }
//
//        // Se loguea, entonces iniciamos timer
//        timer.iniciar();
//
//        // PARTE 3 - GESTION DE PRODUCTOS

    }

    public static void menu() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1. Ingresar cuenta");
        System.out.println("2. Registrar cuenta nueva");
        System.out.println("3. Salir");
        String Opcion = scanner.nextLine();
        switch (Opcion) {
            case "1":
                Login();
                break;
            case "2":
                Registrarse();
                break;
            case "3":
                break;

        }

    }

    public static void Login() {
        Scanner scanner = new Scanner(System.in);
        try {
            Jedis jedis = PoolRedis.getInstancia().getConection();
            if (jedis != null) {
                System.out.println("Ingrese DNI o salir para volver al menu:");
                String dni = scanner.nextLine();
                if (dni.equals("salir")) {
                    menu();
                }
                String claveRedis = "usuario:" + dni;
                while (dni.length() != 8 || dni.contains(" ") || dni.isBlank() || !jedis.exists(dni)) {
                    System.out.println("Error al ingresar el dni");
                    System.out.println("Intentelo nuevamente o ingrese salir para volver al menu");
                    dni = scanner.nextLine();
                    claveRedis = "usuario" + dni;
                    if (dni.equals("salir")) {
                        menu();
                        break;
                    }
                }

                System.out.println("Ingrese la contraseña: ");
                String contrasena = scanner.nextLine();
                claveRedis = "usuario" + dni;
                String contrasenaRedis = jedis.hget(claveRedis, "contraseña");
                while (!contrasena.equals(contrasenaRedis) || contrasena.contains(" ")) {
                    System.out.println("Contraseña incorrecta");
                    System.out.println("Ingrese nuevamente la contraseña o ingrese salir para volver a ingresar un dni: ");
                    contrasena = scanner.nextLine();
                    if (contrasena.equals("salir")) {
                        menu();
                        break;
                    }
                }
            }
        } catch (ErrorConectionRedisException e) {
            throw new RuntimeException(e);
        }

    }

    public static void Registrarse() {
        boolean valido = false;
        Scanner scanner = new Scanner(System.in);
        try {
            Jedis jedis = PoolRedis.getInstancia().getConection();
            if (jedis != null) {
                System.out.println("Ingrese DNI:");
                String dni = scanner.nextLine();
                while (valido) {
                    if (dni.length() != 8 || dni.contains(" ") || dni.isBlank()) {
                        System.out.println("Error al registrar el dni.");
                        System.out.println("Este contiene espacios en blanco o no cumple con la longitud necesaria. \n");

                    }

                    String claveRedis = "usuario:" + dni;

                    if (jedis.exists(claveRedis)) {
                        System.out.println("El dni ingresado ya esta registrado");
                        dni = scanner.nextLine();
                    }

                }

                System.out.println("Ingrese contraseña");
                String contrasena = scanner.nextLine();

                System.out.println("Ingrese Nombre:");
                String nombre = scanner.nextLine();

                System.out.println("Ingrese mail:");
                String mail = scanner.nextLine();

                while (!mail.contains("@")) {
                    System.out.println("Error al registrar el mail");
                    System.out.println("1. Intentarlo nuevamente");
                    System.out.println("2. Volver al menu");
                    String  rta = scanner.nextLine();
                    switch (rta) {
                        case "1":
                            System.out.println("Ingrese nuevamente el mail");
                            mail = scanner.nextLine();
                            break;
                        case "2":
                            menu();
                            break;

                    }

                    Map<String, String> datosUsuario = new HashMap<>();
                    datosUsuario.put("contrasena", contrasena);
                    datosUsuario.put("nombre", nombre);
                    datosUsuario.put("mail", mail);

                    jedis.hset("usuario:" + dni, datosUsuario);

                    scanner.close();
                    menu();

                }
            }
        } catch (ErrorConectionRedisException e) {
            throw new RuntimeException(e);
        }
    }
}