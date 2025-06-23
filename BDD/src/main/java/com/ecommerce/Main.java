package com.ecommerce;

import com.datastax.oss.driver.api.core.CqlSession;
import com.ecommerce.db.Conexiones;
import com.ecommerce.db.PoolMongoDB;
import com.ecommerce.enums.Categoria;
import com.ecommerce.enums.CondicionIVA;
import com.ecommerce.enums.FormaPago;
import com.ecommerce.exceptions.ErrorConectionRedisException;
import com.ecommerce.operaciones.Facturacion;
import com.ecommerce.operaciones.Pago;
import com.ecommerce.pedido.ItemPedido;
import com.ecommerce.pedido.PedidoService;
import com.ecommerce.producto.Producto;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import org.bson.Document;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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

                        if (dni.equals("45073584") && password.equals("123")){
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

        if(admin){
            System.out.println("¡Bienvenido admin:" + usuarioActual.getNombre() + "!");
        }else {
            System.out.println("¡Bienvenido " + usuarioActual.getNombre() + "!");
        }

        Carrito carrito = new Carrito(usuarioActual.getDni(), usuarioActual.getNombre(), usuarioActual.getApellido(), usuarioActual.getMail(), usuarioActual.getCondicionIVA().getValor());

        while(!terminar){
            int opcion, subopcion;
            Stock catalogo;
            switch (admin){
                case true:
                    catalogo = new Stock();

                    System.out.println("\nMENU ADMINISTRATIVO");
                    System.out.println("1. Listar catálogo de productos");
                    System.out.println("2. Agregar productos stock");
                    System.out.println("3. Eliminar productos stock");
                    System.out.println("4. Modificar productos stock");
                    System.out.println("5. Modificar informacion producto");
                    System.out.println("6. Mostrar historial de cambios");
                    System.out.println("7. Mostrar historial de facturas/pago para un usuario");
                    System.out.println("8. Cerrar Sesión");
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

                            System.out.println("Ingrese descuento:");
                            double descuento = scanner.nextDouble();
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
                                    new ArrayList<>(),
                                    descuento,
                                    stockInicial
                            );

                            catalogo.agregarProducto(nuevoProducto, stockInicial, descuento);
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
                            scanner.nextLine();
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
                                    catalogo.modificarCantidadStock(id, nuevaCantidad, usuarioActual.getDni());
                                    break;

                                default:
                                    System.out.println("Opción inválida.");
                                    break;
                            }
                            break;
                        case 5:
                            System.out.println("1. Modificar imagen.");
                            System.out.println("2. Modificar video.");
                            subopcion = scanner.nextInt();
                            scanner.nextLine(); // limpiar newline

                            String campo = null;
                            String tipo = null;

                            if (subopcion == 1) {
                                campo = "imagenes";
                                tipo = "imagen";
                            } else if (subopcion == 2) {
                                campo = "videos";
                                tipo = "video";
                            } else {
                                System.out.println("Elija una opción dentro del rango.");
                                break;
                            }

                            System.out.println("Ingrese el ID del producto al que desea modificar sus " + tipo + "s:");
                            String productoId = scanner.nextLine();

                            Document producto = catalogo.buscarProducto(productoId);
                            if (producto == null) {
                                System.out.println("No se encontró un producto con ese ID.");
                                break;
                            }

                            System.out.println("Ingrese las nuevas URLs de " + tipo + "s (una por línea). Escriba 'fin' para terminar:");

                            List<String> nuevasUrls = new ArrayList<>();
                            while (true) {
                                String entrada = scanner.nextLine().trim();
                                if (entrada.equalsIgnoreCase("fin")) break;
                                if (!entrada.isEmpty()) nuevasUrls.add(entrada);
                            }

                            if (nuevasUrls.isEmpty()) {
                                System.out.println("No se ingresaron nuevas " + tipo + "s.");
                                break;
                            }

                            MongoCollection<Document> coleccion = PoolMongoDB.getInstancia().getConexion("productosDB").getCollection("productos");

                            coleccion.updateOne(eq("id", productoId), set(campo, nuevasUrls));
                            System.out.println("Las " + tipo + "s fueron actualizadas correctamente.");
                            break;
                        case 6:
                            catalogo.listarProductosEnStock();
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

                                    Object valorAnteriorObj = cambio.get("valor_anterior");
                                    Object valorNuevoObj = cambio.get("valor_nuevo");

                                    if (valorAnteriorObj != null) {
                                        try {
                                            double valorAnterior = Double.parseDouble(valorAnteriorObj.toString());
                                            System.out.println("Valor anterior: " + valorAnterior);
                                        } catch (NumberFormatException e) {
                                            System.out.println("Valor anterior: (no numérico)");
                                        }
                                    } else {
                                        System.out.println("Valor anterior: (no disponible)");
                                    }

                                    if (valorNuevoObj != null) {
                                        try {
                                            double valorNuevo = Double.parseDouble(valorNuevoObj.toString());
                                            System.out.println("Valor nuevo: " + valorNuevo);
                                        } catch (NumberFormatException e) {
                                            System.out.println("Valor nuevo: (no numérico)");
                                        }
                                    } else {
                                        System.out.println("Valor nuevo: (no disponible)");
                                    }

                                    System.out.println("Operador: " + cambio.getString("operador"));
                                    System.out.println("\n");
                                }
                            }
                            break;

                        case 7:
                            scanner.nextLine();
                            System.out.println("Ingrese DNI del cliente:");
                            String dni = scanner.nextLine();

                            try (CqlSession session = CqlSession.builder().withKeyspace("ecommerce").withLocalDatacenter("datacenter1").build();) {
                                Facturacion.mostrarFacturas(dni, session);
                                Pago.mostrarPagos(dni, session);
                            } catch (Exception e) {
                                System.out.println("Error al consultar historial: " + e.getMessage());
                            }
                            break;

                        case 8:
                            System.out.println("Terminando sesión...");
                            terminar = true;
                            break;

                        default:
                            System.out.println("Opción no valida!");
                            break;
                    }
                    break;

                default:

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
                                    scanner.nextLine();
                                    System.out.println("Ingrese el ID del producto al que desea agregar un comentario: ");
                                    String productoId = scanner.nextLine();
                                    System.out.println("Ingrese el comentario: ");
                                    String comentario = scanner.nextLine();
                                    catalogo.agregarComentario(productoId, comentario, usuarioActual.getDni());

                                    Document producto = catalogo.buscarProducto(productoId);
                                    if (producto == null) {
                                        System.out.println("Error: No se encontró el producto después de agregar el comentario.");
                                        break;
                                    }

                                    List<String> comentarios = producto.getList("comentarios", String.class);
                                    if (comentarios != null && comentarios.contains(comentario)) {
                                        System.out.println("Comentario agregado correctamente.");
                                    } else {
                                        System.out.println("No se pudo verificar si el comentario fue agregado.");
                                    }                                    break;
                                case 3:
                                    break;
                                default:
                                    System.out.println("Opcion no valida");
                                    break;
                            }
                            break;

                        case 2:
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
                                    carrito.restaurarUltimoEstado(usuarioActual.getDni());
                                    carrito.imprimirCarrito();
                                    break;

                                case 2:
                                    catalogo.listarProductosEnStock();
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
                                        double descuento = producto.getDouble("descuento") != null ? producto.getDouble("descuento") : 0.0;
                                        double subtotal = cantidad * precioUnitario;
                                        double montoDescuento = subtotal * descuento / 100.0;
                                        double subtotalDescontado = subtotal - montoDescuento;
                                        double montoIva = subtotalDescontado * iva;
                                        double total = subtotalDescontado + montoIva;

                                        UUID uuidProducto = UUID.fromString(producto.getString("id"));

                                        ItemPedido item = new ItemPedido(
                                                uuidProducto,
                                                producto.getString("nombre"),
                                                cantidad,
                                                producto.getString("empresa"),
                                                precioUnitario,
                                                subtotal,
                                                iva,
                                                descuento,
                                                total
                                        );

                                        carrito.agregarItem(usuarioActual.getDni(), item);

                                    }
                                    break;

                                case 3:
                                    List<ItemPedido> items = carrito.restaurarUltimoEstado(usuarioActual.getDni());

                                    if (items.isEmpty()) {
                                        System.out.println("El carrito está vacío.");
                                        break;
                                    }

                                    System.out.println("\nProductos en el carrito:");
                                    for (int i = 0; i < items.size(); i++) {
                                        System.out.printf("%d. %s (Cantidad: %d)\n",
                                                i + 1,
                                                items.get(i).getNombreProducto(),
                                                items.get(i).getCantidad());
                                    }

                                    System.out.print("Seleccione el número del producto a eliminar: ");
                                    scanner.nextLine();

                                    int opcionEliminar;
                                    try {
                                        opcionEliminar = scanner.nextInt();
                                    } catch (InputMismatchException e) {
                                        System.out.println("Entrada inválida.");
                                        scanner.nextLine();
                                        break;
                                    }

                                    if (opcionEliminar < 1 || opcionEliminar > items.size()) {
                                        System.out.println("Opción fuera de rango.");
                                        break;
                                    }

                                    UUID idAEliminar = items.get(opcionEliminar - 1).getProductoId();
                                    carrito.eliminarItem(usuarioActual.getDni(), idAEliminar);

                                    System.out.println("Producto eliminado del carrito.");
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
                                    System.out.println("Carrito vaciado.");
                                    break;

                                case 7:
                                    PedidoService pedidoService = PedidoService.getInstancia();
                                    String idPedido = pedidoService.guardarPedido(carrito);

                                    FormaPago formaPago = null;
                                    scanner.nextLine();

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

                                    if (formaPago == FormaPago.TARJETA_CREDITO || formaPago == FormaPago.TARJETA_DEBITO) {
                                        System.out.print("Ingrese el número de la tarjeta (16 dígitos): ");
                                        String numeroTarjeta = scanner.nextLine().trim();
                                        while (!numeroTarjeta.matches("\\d{16}")) {
                                            System.out.print("Número inválido. Ingrese 16 dígitos: ");
                                            numeroTarjeta = scanner.nextLine().trim();
                                        }

                                        System.out.print("Ingrese el nombre del titular: ");
                                        String titular = scanner.nextLine().trim();
                                        while (titular.isBlank()) {
                                            System.out.print("Nombre inválido. Intente de nuevo: ");
                                            titular = scanner.nextLine().trim();
                                        }

                                        System.out.print("Ingrese fecha de vencimiento (MM/AA): ");
                                        String vencimiento = scanner.nextLine().trim();
                                        while (!vencimiento.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                                            System.out.print("Formato inválido. Use MM/AA (ej. 09/27): ");
                                            vencimiento = scanner.nextLine().trim();
                                        }

                                        System.out.print("Ingrese el código de seguridad (CVV, 3 dígitos): ");
                                        String cvv = scanner.nextLine().trim();
                                        while (!cvv.matches("\\d{3}")) {
                                            System.out.print("Código inválido. Ingrese 3 dígitos: ");
                                            cvv = scanner.nextLine().trim();
                                        }

                                        System.out.println("Datos de la tarjeta ingresados correctamente.");
                                    }


                                    try (CqlSession session = CqlSession.builder().withKeyspace("ecommerce").withLocalDatacenter("datacenter1").build()) {

                                        // generar las facturas por empresa y obtener el mapa con los montos
                                        Map<UUID, Double> facturasConMonto = Facturacion.generarFactura(idPedido, formaPago.getDescripcion(), session);

                                        if (!facturasConMonto.isEmpty()) {
                                            //registrar el pago global por todas las facturas
                                            Pago.registrarPago(
                                                    session,
                                                    facturasConMonto,
                                                    usuarioActual.getDni(),
                                                    formaPago.getDescripcion(),
                                                    "SistemaECommerce",
                                                    "COMPLETADO"
                                            );
                                        }

                                    } catch (Exception e) {
                                        System.out.println("Error al facturar y registrar el pago: " + e.getMessage());
                                    }

                                    System.out.println("Pedido convertido y facturado correctamente.");
                                    carrito.getItemsPedido().clear();
                                    terminar = true;
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

        if (usuarioActual != null && !admin) {
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
