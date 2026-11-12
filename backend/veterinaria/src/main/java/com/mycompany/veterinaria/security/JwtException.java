package com.mycompany.veterinaria.security;

/** Se lanza cuando un token JWT no es aceptable. El código distingue la causa para el cliente Android. */
public class JwtException extends Exception {

    public static final String TOKEN_EXPIRADO = "TOKEN_EXPIRADO";
    public static final String TOKEN_INVALIDO = "TOKEN_INVALIDO";

    private final String codigo;

    public JwtException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
