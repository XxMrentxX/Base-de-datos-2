package com.tuempresa;

import com.datastax.oss.driver.api.core.CqlSession;
import com.tuempresa.db.Conexiones;
import com.tuempresa.enums.Categoria;
import com.tuempresa.enums.CondicionIVA;
import com.tuempresa.enums.FormaPago;
import com.tuempresa.exceptions.ErrorConectionRedisException;
import com.tuempresa.operaciones.Facturacion;
import com.tuempresa.operaciones.Pago;
import com.tuempresa.pedido.ItemPedido;
import com.tuempresa.pedido.PedidoService;
import com.tuempresa.producto.Producto;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;

public class Main {
    static {
        // Silenciar MongoDB driver (usa java.util.logging)
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);
    }

    private static Scanner scanner = new Scanner(System.in);
    private static Usuario usuarioActual = null;

    public static void main(String[] args) {
        Conexiones conexiones = new Conexiones();

        conexiones.testRedis();
        conexiones.testNeo4j();
        conexiones.testCassandra();
        conexiones.testMongoDB();

        Timer timer = new Timer();
        Usuario user = new Usuario();

        System.out.println("¡Bienvenido a nuestro E-Commerce!");

        boolean ejecutando = true;
        boolean terminar = false; // TERMINA EL PROGRAMA ENTERO
        boolean admin = false;

        while (ejecutando){
            System.out.println("Por favor seleccione una opcion: ");
            System.out.println("1. LOGIN");
            System.out.println("2. REGISTRARSE");
            System.out.println("3. SALIR");
            int opcion = scanner.nextInt();
            boolean salir;

            switch(opcion){
                case 1:
                    try{
                        System.out.println("Ingrese su DNI (salir para volver al menu):");
                        String dni = scanner.next();
                        if (dni.equalsIgnoreCase("salir")) {
                            break;
                        }

                        System.out.println("Ingrese la contraseña ('salir' para volver al menu): ");
                        scanner.nextLine();
                        String password = scanner.nextLine();
                        if (password.equalsIgnoreCase("salir")) {
                            break;
                        }

                        usuarioActual = Usuario.verificarCredenciales(dni, password);
                        if (usuarioActual != null) {
                            System.out.println("Login exitoso!");
                            ejecutando = false;
                        } else {
                            System.out.println("Credenciales incorrectas. Intente de nuevo.");
                        }

                        if (dni == "45073584" && password == "123"){
                            admin = true;
                        }

                    } catch (ErrorConectionRedisException e) {
                        throw new RuntimeException(e);
                    }
                    break;

                case 2:
                    try{
                        System.out.println("Ingrese su DNI ('salir' para volver al menu):");
                        String dni = scanner.next();
                        if (dni.equalsIgnoreCase("salir")) {
                            break;
                        }

                        while (dni.length() != 8 || dni.contains(" ") || dni.isBlank()) {
                            System.out.println("ERROR: El DNI debe tener una longitud de 8 y no puede contener espacios.");
                            System.out.println("Ingrese su DNI ('salir' para volver al menu):");
                            dni = scanner.next();
                            if (dni.equalsIgnoreCase("salir")) {
                                break;
                            }
                        }

                        scanner.nextLine(); // consume newline
                        System.out.println("Ingrese la contraseña: ");
                        String password = scanner.nextLine();

                        System.out.println("Ingrese su nombre: ");
                        String nombre = scanner.nextLine();

                        System.out.println("Ingrese su apellido: ");
                        String apellido = scanner.nextLine();

                        System.out.println("Ingrese su mail: ");
                        String mail = scanner.nextLine();

                        while (!mail.contains("@") || !mail.contains(".")) {
                            System.out.println("ERROR: El mail debe tener el formato '<a>@<b>.<c>'.");
                            System.out.println("Ingrese nuevamente el mail ('salir' para volver al menu): ");
                            mail = scanner.nextLine();
                            if (mail.equalsIgnoreCase("salir")) {
                                break;
                            }
                        }

                        CondicionIVA condicionSeleccionada = null;

                        while (condicionSeleccionada == null) {
                            System.out.println("OPCIONES: \n--> RESPONSABLE_INSCRIPTO \n--> MONOTRIBUTISA \n--> EXENTO \n--> NO_RESPONSABLE \n--> CONSUMIDOR_FINAL");
                            System.out.println("Seleccione su condición frente al IVA:");
                            for (CondicionIVA c : CondicionIVA.values()) {
                                if (c != CondicionIVA.UNDEFINED) {
                                    System.out.println("- " + c.getValor());
                                }
                            }

                            System.out.print("Ingrese su condición: ");
                            String entrada = scanner.nextLine().trim();

                            condicionSeleccionada = CondicionIVA.fromString(entrada);

                            if (condicionSeleccionada == CondicionIVA.UNDEFINED) {
                                System.out.println("Opción inválida. Intente nuevamente.\n");
                                condicionSeleccionada = null;
                            }
                        }


                        Usuario nuevoUsuario = new Usuario(nombre, apellido, mail, dni, password, condicionSeleccionada, Categoria.UNDEFINED);
                        if (nuevoUsuario.guardar()) {
                            System.out.println("Usuario registrado exitosamente!");
                            usuarioActual = nuevoUsuario;
                            ejecutando = false;
                        } else {
                            System.out.println("Error: El DNI ya está registrado");
                        }

                    } catch (ErrorConectionRedisException e) {
                        throw new RuntimeException(e);
                    }
                    break;

                case 3:
                    ejecutando = false;
                    terminar = true;
                    break;

                default:
                    System.out.println("Opcion no valida");
                    break;
            }
        }


        while(!terminar){
            int opcion, subopcion;
            Stock catalogo;
            switch (admin){
                case true:
                    catalogo = new Stock();
                    System.out.println("¡Bienvenido admin:" + usuarioActual.getNombre() + "!");

                    System.out.println("\nMENU ADMINISTRATIVO");
                    System.out.println("1. Listar catálogo de productos");
                    System.out.println("2. Agregar productos stock");
                    System.out.println("3. Eliminar productos stock");
                    System.out.println("4. Modificar productos stock");
                    System.out.println("5. Mostrar historial de cambios");
                    System.out.println("6. Mostrar historial de facturas/pago para un usuario");
                    System.out.println("7. Cerrar Sesión");
                    opcion = scanner.nextInt();

                    switch (opcion){
                        case 1:
                            catalogo.listarProductosEnStock();
                            break;

                        case 2:
                            scanner.nextLine();

                            System.out.println("Ingrese nombre del producto:");
                            String nombre = scanner.nextLine();

                            System.out.println("Ingrese descripción del producto:");
                            String descripcion = scanner.nextLine();

                            System.out.println("Ingrese empresa fabricante:");
                            String empresa = scanner.nextLine();

                            System.out.println("Ingrese precio del producto:");
                            double precio = scanner.nextDouble();
                            scanner.nextLine();

                            System.out.println("Ingrese stock inicial:");
                            int stockInicial = scanner.nextInt();
                            scanner.nextLine();

                            List<String> imagenes = List.of("imagen.jpg");
                            List<String> videos = List.of("video.mp4");

                            Producto nuevoProducto = new Producto(
                                    UUID.randomUUID().toString(),
                                    nombre,
                                    descripcion,
                                    empresa,
                                    imagenes,
                                    videos,
                                    new ArrayList<>(),
                                    precio,
                                    new ArrayList<>()
                            );

                            catalogo.agregarProducto(nuevoProducto, stockInicial);
                            break;

                        case 3:
                            scanner.nextLine();
                            System.out.println("Ingrese el ID del producto a eliminar: ");
                            String idProducto = scanner.nextLine();

                            if (catalogo.buscarProducto(idProducto) == null) {
                                System.out.println("No existe un producto con ese ID");
                            } else{
                                catalogo.eliminarProductoPorId(idProducto);
                                System.out.println("Producto eliminado");
                            }
                            break;

                        case 4:
                            scanner.nextLine();

                            System.out.println("Ingrese el ID del producto a modificar:");
                            String id = scanner.nextLine();

                            Document prod = catalogo.buscarProducto(id);
                            if (prod == null) {
                                System.out.println("No se encontró un producto con ese ID.");
                                break;
                            }

                            System.out.println("Parámetro a modificar:");
                            System.out.println("1. Precio del producto");
                            System.out.println("2. Cantidad en stock");
                            subopcion = scanner.nextInt();

                            switch (subopcion) {
                                case 1:
                                    System.out.print("Ingrese el nuevo precio: ");
                                    double nuevoPrecio = scanner.nextDouble();
                                    scanner.nextLine();
                                    catalogo.actualizarPrecio(id, nuevoPrecio, usuarioActual.getDni());
                                    break;

                                case 2:
                                    System.out.print("Ingrese la nueva cantidad de stock: ");
                                    int nuevaCantidad = scanner.nextInt();
                                    scanner.nextLine();
                                    catalogo.modificarCantidadStock(id, nuevaCantidad);
                                    break;

                                default:
                                    System.out.println("Opción inválida.");
                                    break;
                            }
                            break;

                        case 5:
                            scanner.nextLine();
                            System.out.println("Ingrese el ID del producto para ver su historial de cambios:");
                            String idProd = scanner.nextLine();

                            Document docProducto = catalogo.buscarProducto(idProd);

                            if (docProducto == null) {
                                System.out.println("No se encontró un producto con ese ID.");
                                break;
                            }

                            List<Document> historial = docProducto.getList("historial_cambios", Document.class);

                            if (historial == null || historial.isEmpty()) {
                                System.out.println("El producto no tiene historial de cambios.");
                            } else {
                                System.out.println("Historial de cambios del producto:");
                                for (Document cambio : historial) {
                                    System.out.println("Fecha: " + cambio.getString("fecha"));
                                    System.out.println("Tipo de cambio: " + cambio.getString("tipo"));
                                    System.out.println("Valor anterior: " + cambio.getString("valor_anterior"));
                                    System.out.println("Valor nuevo: " + cambio.getString("valor_nuevo"));
                                    System.out.println("Operador: " + cambio.getString("operador"));
                                }
                            }
                            break;

                        case 6:
                            scanner.nextLine();
                            System.out.println("Ingrese DNI del cliente:");
                            String dni = scanner.nextLine();

                            try (CqlSession session = CqlSession.builder().build()) {
                                Facturacion.mostrarFacturas(dni, session);
                                Pago.mostrarPagos(dni, session);
                            } catch (Exception e) {
                                System.out.println("Error al consultar historial: " + e.getMessage());
                            }
                            break;

                        case 7:
                            System.out.println("Terminando sesión...");
                            terminar = true;
                            break;

                        default:
                            System.out.println("Opción no valida!");
                            break;
                    }
                    break;

                default:
                    System.out.println("¡Bienvenido " + usuarioActual.getNombre() + "!");
                    timer.iniciar();
                    catalogo = new Stock();

                    System.out.println("\nMENU PRINCIPAL");
                    System.out.println("1. Ver Catálogo de Productos");
                    System.out.println("2. Carrito de Compras");
                    System.out.println("3. Mi Perfil");
                    System.out.println("4. Cerrar Sesión");
                    opcion = scanner.nextInt();
                    scanner.nextLine();

                    switch(opcion){
                        case 1:
                            System.out.println("\nCATÁLOGO DE PRODUCTOS");
                            System.out.println("1. Ver todos los productos");
                            System.out.println("2. Agregar comentario a producto");
                            System.out.println("3. Volver al menú principal");

                            subopcion = scanner.nextInt();


                            switch(subopcion){
                                case 1:
                                    catalogo.listarProductosEnStock();
                                    break;
                                case 2:
                                    System.out.println("Ingrese el ID del producto al que desea agregar un comentario: ");
                                    String productoId = scanner.nextLine();
                                    System.out.println("Ingrese el comentario: ");
                                    String comentario = scanner.nextLine();
                                    catalogo.agregarComentario(productoId, comentario, usuarioActual.getDni());
                                    break;
                                case 3:
                                    break;
                                default:
                                    System.out.println("Opcion no valida");
                                    break;
                            }
                            break;

                        case 2:
                            Carrito carrito = new Carrito(usuarioActual.getDni(), usuarioActual.getNombre(), usuarioActual.getApellido(), usuarioActual.getMail(), usuarioActual.getCondicionIVA().getValor());
                            String productoId;

                            System.out.println("\nCARRITO DE COMPRAS");
                            System.out.println("1. Ver contenido del carrito");
                            System.out.println("2. Agregar producto");
                            System.out.println("3. Eliminar producto");
                            System.out.println("4. Deshacer último cambio");
                            System.out.println("5. Rehacer último cambio");
                            System.out.println("6. Vaciar carrito");
                            System.out.println("7. Proceder al checkout (convertir a pedido)");
                            System.out.println("8. Volver al menú principal");
                            subopcion = scanner.nextInt();
                            switch (subopcion){

                                case 1:
                                    carrito.imprimirCarrito();
                                    break;

                                case 2:
                                    scanner.nextLine();

                                    System.out.println("Ingrese el ID del producto al que desea agregarlo al carrito: ");
                                    productoId = scanner.nextLine();
                                    System.out.println("Ingrese la cantidad: ");
                                    int cantidad = scanner.nextInt();

                                    if (cantidad <= 0) {
                                        System.out.println("ERROR: La cantidad debe ser mayor a 0.");
                                        break;
                                    } else if (catalogo.buscarProducto(productoId).getInteger("stock") < cantidad) {
                                        System.out.println("ERROR: No hay suficientes existencias del producto en stock.");
                                    } else if (catalogo.buscarProducto(productoId) == null) {
                                        System.out.println("ERROR: No existe el producto con el ID especificado.");
                                    } else {

                                        Document producto = catalogo.buscarProducto(productoId);
                                        double iva = 0;
                                        if (usuarioActual.getCondicionIVA() == CondicionIVA.RESPONSABLE_INSCRIPTO || usuarioActual.getCondicionIVA() == CondicionIVA.CONSUMIDOR_FINAL) {
                                            iva = 0.21;
                                        }

                                        double precioUnitario = producto.getDouble("precio_actual");
                                        double subtotal = cantidad * precioUnitario;
                                        double montoDescuento = (subtotal * producto.getDouble("porcentaje_descuento")) / 100;
                                        double subtotalDescuento = subtotal - montoDescuento;
                                        double montoIva = subtotalDescuento * iva;
                                        double total = subtotalDescuento + montoIva;

                                        carrito.agregarItem( usuarioActual.getDni(),
                                                new ItemPedido(
                                                        producto.getString("nombre"),
                                                        producto.getInteger("stock"),
                                                        producto.getString("empresa"),
                                                        precioUnitario,
                                                        subtotal,
                                                        iva,
                                                        producto.getDouble("porcentaje_descuento"),
                                                        total
                                                )
                                        );
                                    }
                                    break;

                                case 3:
                                    System.out.println("Ingrese el ID del producto que desea eliminar del carrito: ");
                                    productoId = scanner.nextLine();
                                    carrito.eliminarItem(usuarioActual.getDni(), UUID.fromString(productoId));
                                    break;

                                case 4:
                                    if (carrito.restaurarEstadoAnterior(usuarioActual.getDni())){
                                        System.out.println("Estado anterior del carrito restaurado");
                                    } else {
                                        System.out.println("No hay estados anteriores");
                                    }
                                    break;

                                case 5:
                                    if (carrito.restaurarEstadoSiguiente(usuarioActual.getDni())){
                                        System.out.println("Estado siguiente del carrito restaurado");
                                    } else {
                                        System.out.println("No hay estados posteriores");
                                    }
                                    break;

                                case 6:
                                    carrito.getItemsPedido().clear();
                                    carrito.guardarEstado(usuarioActual.getDni(), new ArrayList<>());
                                    break;

                                case 7:
                                    PedidoService pedidoService = PedidoService.getInstancia();
                                    String idPedido = pedidoService.guardarPedido(carrito); // REVISAR METODO

                                    FormaPago formaPago = null;
                                    scanner.nextLine(); // limpiar buffer
                                    while (formaPago == null) {
                                        System.out.println("Seleccione forma de pago:");
                                        int i = 1;
                                        for (FormaPago fp : FormaPago.values()) {
                                            System.out.println(i++ + ". " + fp.getDescripcion());
                                        }
                                        System.out.print("Ingrese una opción (número o texto): ");
                                        String opcionPago = scanner.nextLine().trim();
                                        formaPago = FormaPago.fromInput(opcionPago);
                                        if (formaPago == null) {
                                            System.out.println("Opción inválida. Intente de nuevo.");
                                        }
                                    }

                                    CqlSession session = CqlSession.builder().build(); // <---- REVISAR ESTO
                                    Facturacion.generarFactura(idPedido, formaPago.getDescripcion(), session);
                                    System.out.println("Pedido convertido y facturado correctamente.");

                                    break;

                                case 8:
                                    break;
                            }
                            break;

                        case 3:
                            System.out.println("\n===== MI PERFIL =====");
                            System.out.println("Nombre completo : " + usuarioActual.getNombre() + " " + usuarioActual.getApellido());
                            System.out.println("DNI             : " + usuarioActual.getDni());
                            System.out.println("E-mail          : " + usuarioActual.getMail());
                            System.out.println("Condición IVA   : " + usuarioActual.getCondicionIVA().getValor());
                            System.out.println("Categoría       : " + usuarioActual.getCategoria().getValor());
                            System.out.println("Sesiones totales: " + usuarioActual.getSesiones());
                            System.out.println("Tiempo total    : " + usuarioActual.getTiempoTotalMinutos() + " min");
                            System.out.println("=====================\n");
                            break;

                        case 4:
                            System.out.println("Terminando sesion...");
                            terminar = true;
                            break;

                        default:
                            System.out.println("Opcion no valida");
                            break;
                    }
            }
        }

        if (usuarioActual != null) {
            long tiempoSesion = timer.parar();
            try {
                usuarioActual.actualizarTiempoSesion(tiempoSesion);
                usuarioActual.guardar();
                System.out.println("Tiempo de sesión: " + tiempoSesion + " minutos");
                System.out.println("Nueva categoría: " + usuarioActual.getCategoria());
            } catch (ErrorConectionRedisException e) {
                System.out.println("Error al guardar datos de sesión: " + e.getMessage());
            }
        }
    }
}
