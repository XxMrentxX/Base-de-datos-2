package com.tuempresa.db;

import com.tuempresa.exceptions.ErrorConectionRedisException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class PoolRedis {

    private static PoolRedis instancia;
    private JedisPool pool;

    private PoolRedis(){
        pool = new JedisPool("localhost", 6379);

    }

    public static PoolRedis getInstancia(){
        if(instancia == null)
            instancia = new PoolRedis();
        return instancia;

    }

    public Jedis getConection() throws ErrorConectionRedisException{
        try {
            Jedis jedis = pool.getResource();
            return jedis;
        }
        catch (Exception e) {
            throw new ErrorConectionRedisException("Error al conectarse con redis");
        }
    }
}