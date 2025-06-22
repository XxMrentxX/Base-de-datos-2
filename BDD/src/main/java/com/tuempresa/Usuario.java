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


}
