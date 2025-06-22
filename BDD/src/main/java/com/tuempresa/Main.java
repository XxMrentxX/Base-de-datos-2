package com.tuempresa;

import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {

    static {
        // Silenciar MongoDB driver (usa java.util.logging)
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);
    }

    public static void main(String[] args) {

    }

}