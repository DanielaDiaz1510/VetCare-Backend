package com.mycompany.veterinaria.exception;

/** HTTP 429. Demasiados intentos de inicio de sesión fallidos. El filtro de la API la convierte en una respuesta JSON. */
public class DemasiadosIntentosException extends RuntimeException {

    public DemasiadosIntentosException(String mensaje) {
        super(mensaje);
    }
}
