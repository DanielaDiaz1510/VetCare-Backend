package com.mycompany.veterinaria.exception;

/** HTTP 403. No tiene permisos para esta operación. El filtro de la API la convierte en una respuesta JSON. */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
