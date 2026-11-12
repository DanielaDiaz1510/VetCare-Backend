package com.mycompany.veterinaria.security;

import java.util.Set;

/**
 * Política de autorización por roles (RF-03). Está aquí, en un solo lugar y sin dependencias,
 * para poder leerla de un vistazo y probarla con pruebas unitarias.
 *
 * Roles:  ADMIN  y  VETERINARIO.
 *
 *  Recurso                                             ADMIN   VETERINARIO
 *  --------------------------------------------------  ------  -----------
 *  POST /api/login, POST /api/registro                 público (sin token)
 *  GET  /api/me, POST /api/logout                      cualquier usuario autenticado
 *  /api/usuarios, /api/roles, /api/auditoria,
 *  /api/backup                                         SI      403
 *  /api/veterinarios  (crear, editar, eliminar)        SI      403
 *  cualquier DELETE                                    SI      403
 *  GET/POST/PUT sobre el resto de recursos clínicos    SI      SI
 *
 * Regla de oro: DENEGAR POR DEFECTO. Si una ruta no está en esta tabla, nadie entra (403).
 */
public final class Permisos {

    public static final String ADMIN = "ADMIN";
    public static final String VETERINARIO = "VETERINARIO";

    /** Rutas base que solo puede usar un administrador. */
    private static final Set<String> SOLO_ADMIN = Set.of(
            "/api/usuarios", "/api/roles", "/api/auditoria", "/api/backup");

    /** Rutas base abiertas a ambos roles (con la excepción de DELETE y de /api/veterinarios). */
    private static final Set<String> AMBOS_ROLES = Set.of(
            "/api/propietarios", "/api/mascotas", "/api/veterinarios", "/api/citas",
            "/api/historiales", "/api/tratamientos", "/api/vacunas", "/api/vacunaciones",
            "/api/medicamentos", "/api/alergias", "/api/mascota-alergias",
            "/api/estadisticas", "/api/export");

    private Permisos() {
    }

    /** Rutas que NO exigen token. */
    public static boolean esPublica(String metodo, String ruta) {
        String r = normalizar(ruta);
        return "POST".equals(metodo) && (r.equals("/api/login") || r.equals("/api/registro"));
    }

    /** ¿Puede alguien con estos roles ejecutar este método sobre esta ruta? */
    public static boolean permite(String metodo, String ruta, Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        String base = rutaBase(ruta);

        // Cualquier usuario autenticado
        if (base.equals("/api/me") || base.equals("/api/logout")) {
            return true;
        }

        boolean esAdmin = roles.contains(ADMIN);
        boolean esVet = roles.contains(VETERINARIO);

        if (SOLO_ADMIN.contains(base)) {
            return esAdmin;
        }
        if (AMBOS_ROLES.contains(base)) {
            if (esAdmin) {
                return true;
            }
            if (!esVet) {
                return false;
            }
            if ("DELETE".equals(metodo)) {
                return false;                                   // eliminar: solo ADMIN
            }
            boolean lectura = "GET".equals(metodo) || "HEAD".equals(metodo);
            if (base.equals("/api/veterinarios")) {
                return lectura;                                 // gestionar veterinarios: solo ADMIN
            }
            return true;
        }
        return false;                                           // denegar por defecto
    }

    /** "/api/citas/5/cancelar" -> "/api/citas" ; "/api/export/mascotas/pdf" -> "/api/export". */
    static String rutaBase(String ruta) {
        String[] partes = normalizar(ruta).split("/");           // ["", "api", "citas", ...]
        return partes.length >= 3 ? "/" + partes[1] + "/" + partes[2] : normalizar(ruta);
    }

    private static String normalizar(String ruta) {
        if (ruta == null) {
            return "";
        }
        String r = ruta.trim();
        while (r.length() > 1 && r.endsWith("/")) {
            r = r.substring(0, r.length() - 1);
        }
        return r;
    }
}
