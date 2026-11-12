package com.mycompany.veterinaria.controler;

import com.mycompany.veterinaria.exception.ValidacionException;
import com.mycompany.veterinaria.security.UsuarioAutenticado;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Base de los controladores de la API. Aporta lo que todos necesitan:
 * leer parámetros de la URL con errores claros (400) y saber qué usuario hizo la petición.
 *
 * Los controladores NO capturan excepciones: las lanza y el ApiSecurityFilter las convierte en JSON.
 */
public abstract class ApiServlet extends HttpServlet {

    /** Usuario autenticado (lo puso el filtro de seguridad). */
    protected static UsuarioAutenticado usuario(HttpServletRequest req) {
        return UsuarioAutenticado.desde(req);
    }

    protected static Long actor(HttpServletRequest req) {
        return usuario(req).getId();
    }

    /** Parámetro de texto opcional: vacío -> null. */
    protected static String texto(HttpServletRequest req, String nombre) {
        String v = req.getParameter(nombre);
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    /** Parámetro numérico opcional. */
    protected static Long numero(HttpServletRequest req, String nombre) {
        String v = texto(req, nombre);
        if (v == null) {
            return null;
        }
        try {
            return Long.valueOf(v);
        } catch (NumberFormatException e) {
            throw new ValidacionException("El parámetro '" + nombre + "' debe ser un número entero");
        }
    }

    /** Parámetro numérico obligatorio. */
    protected static Long numeroObligatorio(HttpServletRequest req, String nombre) {
        Long v = numero(req, nombre);
        if (v == null) {
            throw new ValidacionException("Falta el parámetro '" + nombre + "'");
        }
        return v;
    }

    /** Parámetro de fecha opcional con formato AAAA-MM-DD. */
    protected static LocalDate fecha(HttpServletRequest req, String nombre) {
        String v = texto(req, nombre);
        if (v == null) {
            return null;
        }
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            throw new ValidacionException("El parámetro '" + nombre + "' debe tener formato AAAA-MM-DD");
        }
    }

    /** Parámetro entero opcional con valor por defecto. */
    protected static int entero(HttpServletRequest req, String nombre, int porDefecto) {
        Long v = numero(req, nombre);
        return v == null ? porDefecto : v.intValue();
    }
}
