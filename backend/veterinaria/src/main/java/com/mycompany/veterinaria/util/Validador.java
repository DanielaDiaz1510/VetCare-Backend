package com.mycompany.veterinaria.util;

import com.mycompany.veterinaria.exception.ValidacionException;
import java.util.regex.Pattern;

/**
 * Validaciones del lado servidor (RNF-07). Aunque la app Android ya valide los formularios,
 * el servidor NUNCA debe confiar en el cliente: cualquiera puede llamar a la API sin la app.
 */
public final class Validador {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");
    private static final Pattern DOCUMENTO = Pattern.compile("^[A-Za-z0-9\\-]{4,30}$");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._\\-]{3,50}$");

    private Validador() {
    }

    public static String requerido(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("El campo '" + campo + "' es obligatorio");
        }
        String v = valor.trim();
        if (v.length() > max) {
            throw new ValidacionException("El campo '" + campo + "' no puede superar " + max + " caracteres");
        }
        return v;
    }

    /** Devuelve null si viene vacío; si viene lleno, valida el largo. */
    public static String opcional(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return requerido(valor, campo, max);
    }

    public static String emailOpcional(String valor) {
        String v = opcional(valor, "email", 120);
        if (v != null && !EMAIL.matcher(v).matches()) {
            throw new ValidacionException("El correo electrónico no tiene un formato válido");
        }
        return v == null ? null : v.toLowerCase();
    }

    public static String emailRequerido(String valor) {
        String v = emailOpcional(valor);
        if (v == null) {
            throw new ValidacionException("El campo 'email' es obligatorio");
        }
        return v;
    }

    public static String telefonoOpcional(String valor) {
        String v = opcional(valor, "telefono", 20);
        if (v != null && !TELEFONO.matcher(v).matches()) {
            throw new ValidacionException("El teléfono solo puede tener números, espacios, + - ( ) (7 a 20 caracteres)");
        }
        return v;
    }

    public static String documento(String valor) {
        String v = requerido(valor, "numeroDocumento", 30);
        if (!DOCUMENTO.matcher(v).matches()) {
            throw new ValidacionException("El número de documento debe tener entre 4 y 30 letras, números o guiones");
        }
        return v;
    }

    public static String username(String valor) {
        String v = requerido(valor, "username", 50);
        if (!USERNAME.matcher(v).matches()) {
            throw new ValidacionException("El usuario debe tener entre 3 y 50 caracteres: letras, números, punto, guion o guion bajo");
        }
        return v;
    }

    /** Política de contraseñas: mínimo 8 caracteres con al menos una letra y un número. */
    public static String password(String valor) {
        if (valor == null || valor.length() < 8) {
            throw new ValidacionException("La contraseña debe tener al menos 8 caracteres");
        }
        if (valor.length() > 72) {                       // BCrypt solo usa los primeros 72 bytes
            throw new ValidacionException("La contraseña no puede superar 72 caracteres");
        }
        boolean letra = valor.chars().anyMatch(Character::isLetter);
        boolean numero = valor.chars().anyMatch(Character::isDigit);
        if (!letra || !numero) {
            throw new ValidacionException("La contraseña debe contener al menos una letra y un número");
        }
        return valor;
    }

    /** Verifica que el valor sea uno de los permitidos (sin importar mayúsculas) y lo devuelve en mayúsculas. */
    public static String uno(String valor, String campo, String... permitidos) {
        String v = requerido(valor, campo, 30).toUpperCase();
        for (String p : permitidos) {
            if (p.equals(v)) {
                return v;
            }
        }
        throw new ValidacionException("El campo '" + campo + "' debe ser uno de: " + String.join(", ", permitidos));
    }

    public static void positivo(Double valor, String campo) {
        if (valor != null && valor <= 0) {
            throw new ValidacionException("El campo '" + campo + "' debe ser mayor que cero");
        }
    }
}
