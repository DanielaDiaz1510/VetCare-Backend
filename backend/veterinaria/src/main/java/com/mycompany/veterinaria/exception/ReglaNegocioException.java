package com.mycompany.veterinaria.exception;

/** HTTP 409. Una regla de negocio impide la operación. El filtro de la API la convierte en una respuesta JSON. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
