package com.tuempresa;

import com.tuempresa.db.PoolRedis;
import com.tuempresa.exceptions.ErrorConectionRedisException;
import redis.clients.jedis.Jedis;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Usuario {
    private String nombre;
    private String mail;
    private String dni;
    private String contraseña;
    private String categoria;

    public static void Login() {
        Scanner scanner = new Scanner(System.in);
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(OutputStream.nullOutputStream()));
            Jedis jedis = PoolRedis.getInstancia().getConection();
            System.setOut(originalOut);
            if (jedis != null) {
                System.out.println("Ingrese DNI o salir para volver al menu:");
                String dni = scanner.nextLine();
                if (dni.equals("salir")) {
                    Main.menuInicio();
                }
                String claveRedis = "usuario:" + dni;
                while (dni.length() != 8 || dni.contains(" ") || dni.isBlank() || !jedis.exists(dni)) {
                    System.out.println("Error al ingresar el dni");
                    System.out.println("Intentelo nuevamente o ingrese salir para volver al menu");
                    dni = scanner.nextLine();
                    claveRedis = "usuario" + dni;
                    if (dni.equals("salir")) {
                        Main.menuInicio();
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
                        Main.menuInicio();
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
                    } else {
                        valido = true;
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
                            Main.menuInicio();
                            break;

                    }

                    Map<String, String> datosUsuario = new HashMap<>();
                    datosUsuario.put("contrasena", contrasena);
                    datosUsuario.put("nombre", nombre);
                    datosUsuario.put("mail", mail);

                    jedis.hset("usuario:" + dni, datosUsuario);

                    scanner.close();
                    Main.menuInicio();

                }
            }
        } catch (ErrorConectionRedisException e) {
            throw new RuntimeException(e);
        }
    }
    public static void menuPrincipal (){
        System.out.println("Menu principal");
        System.out.println("1. Gestionar productos");
        System.out.println("2. Gestionar carrito");
        System.out.println("3. Cerrar sesión");

        // CUANDO CIERRA SESION, USAR TIMER PARA ASIGNAR CATEGORIA A USUARIO


    }

    
}
