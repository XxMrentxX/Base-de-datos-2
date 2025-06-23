package com.ecommerce;

import com.ecommerce.db.PoolRedis;
import com.ecommerce.enums.Categoria;
import com.ecommerce.enums.CondicionIVA;
import com.ecommerce.exceptions.ErrorConectionRedisException;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.Set;

public class Usuario {
    private String nombre;
    private String apellido;
    private String mail;
    private String dni;
    private String password;
    private CondicionIVA condicionIVA;
    private Categoria categoria;
    private long tiempoTotalMinutos;
    private int sesiones;


    public Usuario() {
        this.condicionIVA = CondicionIVA.UNDEFINED;
        this.categoria = Categoria.UNDEFINED;
        this.tiempoTotalMinutos = 0;
        this.sesiones = 0;
    }

    public Usuario(String nombre, String apellido, String mail, String dni, String password, CondicionIVA condicionIVA, Categoria categoria) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.mail = mail;
        this.dni = dni;
        this.password = password;
        this.condicionIVA = condicionIVA;
        this.categoria = categoria;
        this.tiempoTotalMinutos = 0;
        this.sesiones = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public CondicionIVA getCondicionIVA() {
        return condicionIVA;
    }

    public void setCondicionIVA(CondicionIVA condicionIVA) {
        this.condicionIVA = condicionIVA;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public long getTiempoTotalMinutos() {
        return tiempoTotalMinutos;
    }

    public void setTiempoTotalMinutos(long tiempoTotalMinutos) {
        this.tiempoTotalMinutos = tiempoTotalMinutos;
    }

    public int getSesiones() {
        return sesiones;
    }

    public void setSesiones(int sesiones) {
        this.sesiones = sesiones;
    }

    public void actualizarTiempoSesion(long minutos) {
        this.tiempoTotalMinutos += minutos;
        this.sesiones++;
        actualizarCategoria();
    }

    private void actualizarCategoria() {
        double promedioMinutos = this.sesiones > 0 ? (double) this.tiempoTotalMinutos / this.sesiones : 0;

        if (promedioMinutos >= 240) {
            this.categoria = Categoria.TOP;
        } else if (promedioMinutos >= 120) {
            this.categoria = Categoria.MEDIUM;
        } else {
            this.categoria = Categoria.LOW;
        }
    }


    public static boolean existeEmail(String email) throws ErrorConectionRedisException {
        try (Jedis jedis = PoolRedis.getInstancia().getConection()) {
            Set<String> userKeys = jedis.keys("usuario:*");

            for (String key : userKeys) {
                String storedEmail = jedis.hget(key, "mail");
                if (email.equalsIgnoreCase(storedEmail)) {
                    return true;
                }
            }
            return false;
        }
    }


    public boolean guardar() throws ErrorConectionRedisException {
        try (Jedis jedis = PoolRedis.getInstancia().getConection()) {
            String claveRedis = "usuario:" + this.dni;

            if (!jedis.exists(claveRedis)) {
                if (existeEmail(this.mail)) {
                    return false;
                }

                Map<String, String> datosUsuario = Map.of(
                        "nombre", this.nombre,
                        "apellido", this.apellido,
                        "mail", this.mail,
                        "contraseña", this.password,
                        "condicionIVA", this.condicionIVA.getValor(),
                        "categoria", this.categoria.getValor(),
                        "tiempoTotal", String.valueOf(this.tiempoTotalMinutos),
                        "sesiones", String.valueOf(this.sesiones)
                );
                jedis.hset(claveRedis, datosUsuario);
            } else {
                Map<String, String> datosUsuario = Map.of( // actualizar solo la data de sesion
                        "condicionIVA", this.condicionIVA.getValor(),
                        "categoria", this.categoria.getValor(),
                        "tiempoTotal", String.valueOf(this.tiempoTotalMinutos),
                        "sesiones", String.valueOf(this.sesiones)
                );
                jedis.hset(claveRedis, datosUsuario);
            }
            return true;
        }
    }


    public static Usuario cargar(String dni) throws ErrorConectionRedisException {
        try (Jedis jedis = PoolRedis.getInstancia().getConection()) {
            String claveRedis = "usuario:" + dni;


            if (!jedis.exists(claveRedis)) {
                return null;
            }

            Map<String, String> datosUsuario = jedis.hgetAll(claveRedis);

            Usuario usuario = new Usuario();
            usuario.setDni(dni);
            usuario.setNombre(datosUsuario.get("nombre"));
            usuario.setApellido(datosUsuario.get("apellido"));
            usuario.setMail(datosUsuario.get("mail"));
            usuario.setPassword(datosUsuario.get("contraseña"));

            String condicionIVAStr = datosUsuario.getOrDefault("condicionIVA", "undefined");
            usuario.setCondicionIVA(CondicionIVA.fromString(condicionIVAStr));

            String categoriaStr = datosUsuario.getOrDefault("categoria", "undefined");
            usuario.setCategoria(Categoria.fromString(categoriaStr));

            return usuario;
        }
    }

    public static Usuario verificarCredenciales(String dni, String password) throws ErrorConectionRedisException {
        Usuario usuario = cargar(dni);
        if (usuario != null && usuario.getPassword().equals(password)) {
            return usuario;
        }
        return null;
    }
}

