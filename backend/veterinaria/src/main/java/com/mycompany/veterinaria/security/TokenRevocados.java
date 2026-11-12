package com.mycompany.veterinaria.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lista de tokens cerrados (logout). Un JWT no se puede "borrar" porque el servidor no lo guarda,
 * así que al cerrar sesión se anota su identificador (jti) aquí hasta que el token expire de todos modos.
 *
 * Limitación conocida: vive en memoria; si se reinicia el servidor la lista se pierde (los tokens
 * revocados volverían a valer hasta su expiración natural). Para producción se guardaría en BD o Redis.
 */
public final class TokenRevocados {

    private static final Map<String, Long> REVOCADOS = new ConcurrentHashMap<>();

    private TokenRevocados() {
    }

    public static void revocar(String jti, long expiraEpoch) {
        limpiar();
        if (jti != null && !jti.isEmpty()) {
            REVOCADOS.put(jti, expiraEpoch);
        }
    }

    public static boolean estaRevocado(String jti) {
        return jti != null && REVOCADOS.containsKey(jti);
    }

    /** Elimina los que ya expiraron: dejarían de ser válidos igualmente. */
    private static void limpiar() {
        long ahora = Instant.now().getEpochSecond();
        REVOCADOS.values().removeIf(exp -> exp <= ahora);
    }

    /** Solo para pruebas. */
    public static void vaciar() {
        REVOCADOS.clear();
    }
}
