package com.mycompany.veterinaria.exception;

/** HTTP 401. Credenciales incorrectas o token ausente. El filtro de la API la convierte en una respuesta JSON. */
public class NoAutenticadoException extends RuntimeException {

    public NoAutenticadoException(String mensaje) {
        super(mensaje);
    }
}
