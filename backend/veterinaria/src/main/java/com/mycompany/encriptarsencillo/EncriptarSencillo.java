
package com.mycompany.encriptarsencillo;

import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author User
 */
public class EncriptarSencillo {

    public static void main(String[] args) {
        System.out.println("Aqui va: ");
        System.out.println(BCrypt.hashpw("admin123", BCrypt.gensalt()));
    }
}