package com.mycompany.veterinaria.exception;

/** HTTP 400. Los datos enviados no son válidos. El filtro de la API la convierte en una respuesta JSON. */
public class ValidacionException extends RuntimeException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
