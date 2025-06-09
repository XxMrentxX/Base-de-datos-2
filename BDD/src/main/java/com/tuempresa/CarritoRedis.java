package com.tuempresa;
import redis.clients.jedis.Jedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CarritoRedis {

    private final Jedis jedis = new Jedis("localhost");
    private final ObjectMapper mapper = new ObjectMapper(); // Uso del mapper para poder pasar los objetos como JSON.

    // Agrega producto al carrito
    public void agregarProducto(String usuarioId, ProductoEnCarrito producto) throws Exception {
        String key = "carrito:" + usuarioId;
        List<ProductoEnCarrito> carrito = obtenerCarrito(usuarioId);

        // Si existe el producto aumenta la cantidad
        boolean existe = false;
        for (ProductoEnCarrito p : carrito) {
            if (p.getProducto_id().equals(producto.getProducto_id())) {
                p.setCantidad(p.getCantidad() + producto.getCantidad());
                existe = true;
                break;
            }
        }
        if (!existe) {
            carrito.add(producto);
        }

        // Guardar carrito actualizado
        jedis.set(key, mapper.writeValueAsString(carrito));
        jedis.expire(key, 18000); // Expiración del carrito después de 5 horas
    }

    // Obtener carrito
    public List<ProductoEnCarrito> obtenerCarrito(String usuarioId) throws Exception {
        String key = "carrito:" + usuarioId;
        String carritoJson = jedis.get(key);

        if (carritoJson != null) {
            return mapper.readValue(carritoJson, new TypeReference<List<ProductoEnCarrito>>() {});
        }
        return new ArrayList<>();
    }

    // Eliminar producto del carrito
    public void eliminarProducto(String usuarioId, String productoId) throws Exception {
        String key = "carrito:" + usuarioId;
        List<ProductoEnCarrito> carrito = obtenerCarrito(usuarioId);

        carrito = carrito.stream()
                .filter(p -> !p.getProducto_id().equals(productoId))
                .collect(Collectors.toList());

        jedis.set(key, mapper.writeValueAsString(carrito));
        jedis.expire(key, 3600);
    }

    // Modificar cantidad de un producto en carrito
    public void modificarCantidad(String usuarioId, String productoId, int nuevaCantidad) throws Exception {
        if (nuevaCantidad <= 0) {
            eliminarProducto(usuarioId, productoId);
            return;
        }

        String key = "carrito:" + usuarioId;
        List<ProductoEnCarrito> carrito = obtenerCarrito(usuarioId);

        for (ProductoEnCarrito p : carrito) {
            if (p.getProducto_id().equals(productoId)) {
                p.setCantidad(nuevaCantidad);
                break;
            }
        }

        jedis.set(key, mapper.writeValueAsString(carrito));
        jedis.expire(key, 3600);
    }

    // Limpiar carrito
    public void limpiarCarrito(String usuarioId) {
        String key = "carrito:" + usuarioId;
        jedis.del(key);
    }
}
