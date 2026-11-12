package com.mycompany.veterinaria.util;

/**
 * Lee la configuración del sistema sin dejar secretos escritos en el código.
 *
 * Orden de búsqueda para una clave como "DB_PASSWORD":
 *   1) propiedad de la JVM:        -DDB_PASSWORD=...
 *   2) variable de entorno:        DB_PASSWORD=...
 *   3) valor por defecto (el que se pasa en el código, pensado solo para desarrollo)
 */
public final class Config {

    private Config() {
    }

    public static String get(String clave, String porDefecto) {
        String valor = System.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            valor = System.getenv(clave);
        }
        return (valor == null || valor.isBlank()) ? porDefecto : valor;
    }

    public static long getLong(String clave, long porDefecto) {
        try {
            return Long.parseLong(get(clave, String.valueOf(porDefecto)).trim());
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }
}
