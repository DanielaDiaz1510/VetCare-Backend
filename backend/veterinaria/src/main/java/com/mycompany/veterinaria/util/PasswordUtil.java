package com.mycompany.veterinaria.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    public static String cifrar(String passwordPlano) {
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt());
    }

    public static boolean verificar(String passwordPlano, String hash) {
        return BCrypt.checkpw(passwordPlano, hash);
    }
}
