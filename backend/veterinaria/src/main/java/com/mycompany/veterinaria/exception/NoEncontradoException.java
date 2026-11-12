package com.mycompany.veterinaria.exception;

/** HTTP 404. El recurso solicitado no existe. El filtro de la API la convierte en una respuesta JSON. */
public class NoEncontradoException extends RuntimeException {

    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
