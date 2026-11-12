package com.mycompany.veterinaria.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mycompany.veterinaria.exception.ValidacionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Validaciones del servidor (RNF-07). */
class ValidadorTest {

    @ParameterizedTest
    @ValueSource(strings = {"abc12345", "Clave2026", "pass1234", "x9y8z7w6"})
    void contrasenaValida(String p) {
        assertEquals(p, Validador.password(p));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "corta1", "soloLetrasLargas", "123456789", "        "})
    void contrasenaInvalida(String p) {
        assertThrows(ValidacionException.class, () -> Validador.password(p));
    }

    @Test
    void contrasenaNula() {
        assertThrows(ValidacionException.class, () -> Validador.password(null));
    }

    @Test
    void contrasenaDemasiadoLarga() {
        assertThrows(ValidacionException.class, () -> Validador.password("a1".repeat(40)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"juan@correo.com", "ana.perez+vet@clinica.co", "A@B.CO"})
    void correoValido(String c) {
        assertEquals(c.toLowerCase(), Validador.emailRequerido(c));
    }

    @ParameterizedTest
    @ValueSource(strings = {"sinarroba", "a@b", "@correo.com", "a b@c.com", "a@b.c"})
    void correoInvalido(String c) {
        assertThrows(ValidacionException.class, () -> Validador.emailRequerido(c));
    }

    @Test
    void correoOpcionalVacio() {
        assertNull(Validador.emailOpcional(null));
        assertNull(Validador.emailOpcional("   "));
    }

    @Test
    void requeridoYLargo() {
        assertEquals("hola", Validador.requerido("  hola ", "campo", 10));
        assertThrows(ValidacionException.class, () -> Validador.requerido("  ", "campo", 10));
        assertThrows(ValidacionException.class, () -> Validador.requerido(null, "campo", 10));
        assertThrows(ValidacionException.class, () -> Validador.requerido("x".repeat(11), "campo", 10));
    }

    @Test
    void valoresPermitidos() {
        assertEquals("ACTIVO", Validador.uno("activo", "estado", "ACTIVO", "INACTIVO"));
        assertThrows(ValidacionException.class, () -> Validador.uno("borrado", "estado", "ACTIVO", "INACTIVO"));
    }

    @Test
    void usernameYDocumento() {
        assertEquals("ana_p", Validador.username("ana_p"));
        assertThrows(ValidacionException.class, () -> Validador.username("a"));
        assertThrows(ValidacionException.class, () -> Validador.username("con espacio"));
        assertThrows(ValidacionException.class, () -> Validador.username("<script>"));
        assertEquals("1020304050", Validador.documento("1020304050"));
        assertThrows(ValidacionException.class, () -> Validador.documento("12"));
        assertThrows(ValidacionException.class, () -> Validador.documento("12; DROP TABLE"));
    }

    @Test
    void telefonoYPeso() {
        assertNull(Validador.telefonoOpcional(""));
        assertEquals("+57 300 123 4567", Validador.telefonoOpcional("+57 300 123 4567"));
        assertThrows(ValidacionException.class, () -> Validador.telefonoOpcional("abc"));
        assertThrows(ValidacionException.class, () -> Validador.positivo(0.0, "peso"));
        assertThrows(ValidacionException.class, () -> Validador.positivo(-3.0, "peso"));
        Validador.positivo(null, "peso");
        Validador.positivo(2.5, "peso");
    }
}
