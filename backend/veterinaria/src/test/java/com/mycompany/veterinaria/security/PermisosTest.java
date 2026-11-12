package com.mycompany.veterinaria.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Matriz de autorización por roles (RF-03): qué puede hacer cada rol y qué recibe un 403. */
class PermisosTest {

    private static final Set<String> ADMIN = Set.of("ADMIN");
    private static final Set<String> VET = Set.of("VETERINARIO");

    @ParameterizedTest(name = "{0} {1} -> ADMIN permitido")
    @CsvSource({
        "GET,/api/usuarios", "POST,/api/usuarios", "PUT,/api/usuarios",
        "GET,/api/roles", "POST,/api/roles", "GET,/api/auditoria", "GET,/api/backup",
        "POST,/api/veterinarios", "PUT,/api/veterinarios", "DELETE,/api/veterinarios",
        "GET,/api/mascotas", "POST,/api/mascotas", "DELETE,/api/mascotas",
        "DELETE,/api/propietarios", "POST,/api/citas", "POST,/api/citas/5/cancelar",
        "DELETE,/api/historiales", "GET,/api/export/mascotas/excel", "GET,/api/estadisticas"
    })
    void adminPuedeTodo(String metodo, String ruta) {
        assertTrue(Permisos.permite(metodo, ruta, ADMIN));
    }

    @ParameterizedTest(name = "{0} {1} -> VETERINARIO permitido")
    @CsvSource({
        "GET,/api/propietarios", "POST,/api/propietarios", "PUT,/api/propietarios",
        "GET,/api/mascotas", "POST,/api/mascotas", "PUT,/api/mascotas",
        "GET,/api/citas", "POST,/api/citas", "PUT,/api/citas", "POST,/api/citas/5/cancelar", "POST,/api/citas/5/atender",
        "GET,/api/historiales", "POST,/api/historiales", "PUT,/api/historiales",
        "POST,/api/tratamientos", "POST,/api/vacunaciones", "POST,/api/mascota-alergias",
        "GET,/api/vacunas", "GET,/api/medicamentos", "GET,/api/alergias",
        "GET,/api/veterinarios", "GET,/api/estadisticas", "GET,/api/export/mascotas/pdf",
        "GET,/api/me", "POST,/api/logout"
    })
    void veterinarioPuedeOperar(String metodo, String ruta) {
        assertTrue(Permisos.permite(metodo, ruta, VET));
    }

    @ParameterizedTest(name = "{0} {1} -> VETERINARIO recibe 403")
    @CsvSource({
        "GET,/api/usuarios", "POST,/api/usuarios", "PUT,/api/usuarios",
        "GET,/api/roles", "POST,/api/roles", "GET,/api/auditoria", "GET,/api/backup",
        "POST,/api/veterinarios", "PUT,/api/veterinarios", "DELETE,/api/veterinarios",
        "DELETE,/api/mascotas", "DELETE,/api/propietarios", "DELETE,/api/citas",
        "DELETE,/api/historiales", "DELETE,/api/tratamientos", "DELETE,/api/vacunaciones",
        "DELETE,/api/vacunas", "DELETE,/api/medicamentos", "DELETE,/api/alergias", "DELETE,/api/mascota-alergias"
    })
    void veterinarioDenegado(String metodo, String ruta) {
        assertFalse(Permisos.permite(metodo, ruta, VET));
    }

    @Test
    @DisplayName("Denegar por defecto: una ruta desconocida no la puede usar nadie")
    void rutaDesconocida() {
        assertFalse(Permisos.permite("GET", "/api/nueva-cosa", ADMIN));
        assertFalse(Permisos.permite("GET", "/api/nueva-cosa", VET));
        assertFalse(Permisos.permite("GET", "/api", ADMIN));
        assertFalse(Permisos.permite("GET", "", ADMIN));
        assertFalse(Permisos.permite("GET", null, ADMIN));
    }

    @Test
    @DisplayName("Sin roles, o con un rol que no existe en la política, no se permite nada")
    void sinRolesORolDesconocido() {
        assertFalse(Permisos.permite("GET", "/api/mascotas", Set.of()));
        assertFalse(Permisos.permite("GET", "/api/mascotas", null));
        assertFalse(Permisos.permite("GET", "/api/mascotas", Set.of("INVITADO")));
        assertFalse(Permisos.permite("GET", "/api/mascotas", Set.of("admin")));     // distingue mayúsculas
        assertFalse(Permisos.permite("GET", "/api/me", Set.of()));
    }

    @Test
    @DisplayName("Una barra final o rutas parecidas no sirven para saltarse la política")
    void trucosDeRuta() {
        assertFalse(Permisos.permite("GET", "/api/usuarios/", VET));
        assertFalse(Permisos.permite("GET", "/api/usuarios//", VET));
        assertFalse(Permisos.permite("GET", "/api/usuariosX", VET));
        assertFalse(Permisos.permite("GET", "/api/usuarios;x=1", VET));
        assertFalse(Permisos.permite("GET", "/API/usuarios", VET));
        assertTrue(Permisos.permite("GET", "/api/mascotas/", VET));
    }

    @Test
    @DisplayName("Un usuario con ambos roles obtiene los permisos del ADMIN")
    void ambosRoles() {
        assertTrue(Permisos.permite("GET", "/api/usuarios", Set.of("ADMIN", "VETERINARIO")));
    }

    @Test
    @DisplayName("Solo login y registro (POST) son públicos")
    void rutasPublicas() {
        assertTrue(Permisos.esPublica("POST", "/api/login"));
        assertTrue(Permisos.esPublica("POST", "/api/registro"));
        assertTrue(Permisos.esPublica("POST", "/api/login/"));
        assertFalse(Permisos.esPublica("GET", "/api/login"));
        assertFalse(Permisos.esPublica("PUT", "/api/registro"));
        assertFalse(Permisos.esPublica("POST", "/api/usuarios"));
        assertFalse(Permisos.esPublica("POST", "/api/logout"));
        assertFalse(Permisos.esPublica("GET", "/api/me"));
        assertFalse(Permisos.esPublica("POST", "/api/login/otra"));
        assertFalse(Permisos.esPublica("POST", null));
    }

    @Test
    @DisplayName("rutaBase reduce la ruta al recurso")
    void rutaBase() {
        assertEquals("/api/citas", Permisos.rutaBase("/api/citas/5/cancelar"));
        assertEquals("/api/export", Permisos.rutaBase("/api/export/mascotas/excel"));
        assertEquals("/api/mascotas", Permisos.rutaBase("/api/mascotas"));
    }
}
