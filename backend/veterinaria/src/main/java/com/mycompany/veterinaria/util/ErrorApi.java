package com.mycompany.veterinaria.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mycompany.veterinaria.exception.AccesoDenegadoException;
import com.mycompany.veterinaria.exception.DemasiadosIntentosException;
import com.mycompany.veterinaria.exception.NoAutenticadoException;
import com.mycompany.veterinaria.exception.NoEncontradoException;
import com.mycompany.veterinaria.exception.ReglaNegocioException;
import com.mycompany.veterinaria.exception.ValidacionException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce cualquier error a una respuesta HTTP con un JSON uniforme (RNF-08):
 *
 *   { "status": 409, "codigo": "REGLA_NEGOCIO", "error": "mensaje legible", "ruta": "/api/citas" }
 *
 * La app Android solo tiene que leer "error" para mostrarlo al usuario y "codigo" para decidir qué hacer.
 *
 *   400 datos inválidos | 401 sin sesión | 403 sin permiso | 404 no existe | 409 conflicto
 *   429 demasiados intentos | 500 fallo interno
 */
public final class ErrorApi {

    private ErrorApi() {
    }

    public static void responder(HttpServletRequest req, HttpServletResponse resp,
            int status, String codigo, String mensaje) throws IOException {
        if (resp.isCommitted()) {
            return;
        }
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("status", status);
        cuerpo.put("codigo", codigo);
        cuerpo.put("error", mensaje);
        cuerpo.put("ruta", req.getRequestURI());
        resp.reset();
        Json.responder(resp, status, cuerpo);
    }

    /**
     * Decide el código HTTP según el tipo de excepción. Las excepciones suelen venir envueltas unas dentro
     * de otras, así que se recorre la cadena de causas y se clasifica con la PRIMERA que se reconozca.
     */
    public static void manejar(Exception e, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        for (Throwable t = e; t != null; t = (t.getCause() == t ? null : t.getCause())) {
            if (t instanceof ValidacionException || t instanceof IllegalArgumentException) {
                responder(req, resp, 400, "DATOS_INVALIDOS", mensajeSeguro(t, "Datos inválidos"));
                return;
            }
            if (t instanceof JsonProcessingException) {
                responder(req, resp, 400, "JSON_INVALIDO",
                        "El cuerpo de la petición no es un JSON válido o tiene valores con formato incorrecto (revise fechas y números)");
                return;
            }
            if (t instanceof NoEncontradoException) {
                responder(req, resp, 404, "NO_ENCONTRADO", t.getMessage());
                return;
            }
            if (t instanceof NoAutenticadoException) {
                responder(req, resp, 401, "CREDENCIALES_INVALIDAS", t.getMessage());
                return;
            }
            if (t instanceof DemasiadosIntentosException) {
                responder(req, resp, 429, "DEMASIADOS_INTENTOS", t.getMessage());
                return;
            }
            if (t instanceof AccesoDenegadoException) {
                responder(req, resp, 403, "ACCESO_DENEGADO", t.getMessage());
                return;
            }
            if (t instanceof ReglaNegocioException) {
                responder(req, resp, 409, "REGLA_NEGOCIO", t.getMessage());
                return;
            }
            if (t instanceof SQLException) {
                SQLException sql = (SQLException) t;
                if (sql.getSQLState() != null && sql.getSQLState().startsWith("23")) {
                    responder(req, resp, 409, "CONFLICTO_DATOS", mensajeIntegridad(sql));
                    return;
                }
            }
        }
        // No se devuelve el detalle técnico al cliente (podría revelar tablas, rutas, etc.)
        e.printStackTrace();
        responder(req, resp, 500, "ERROR_INTERNO", "Ocurrió un error interno. Intente de nuevo más tarde");
    }

    private static String mensajeSeguro(Throwable t, String porDefecto) {
        return (t.getMessage() == null || t.getMessage().isBlank()) ? porDefecto : t.getMessage();
    }

    private static String mensajeIntegridad(SQLException sql) {
        int codigo = sql.getErrorCode();
        if (codigo == 1062) {
            return "Ya existe un registro con ese dato único (por ejemplo correo, usuario o documento)";
        }
        if (codigo == 1451) {
            return "No se puede eliminar: el registro tiene información asociada";
        }
        if (codigo == 1452) {
            return "Referencia inválida: el registro relacionado no existe";
        }
        return "La operación viola una restricción de integridad de la base de datos";
    }
}
