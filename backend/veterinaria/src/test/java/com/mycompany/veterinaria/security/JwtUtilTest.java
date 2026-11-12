package com.mycompany.veterinaria.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas del token JWT: qué acepta y, sobre todo, qué debe RECHAZAR el servidor. */
class JwtUtilTest {

    private static final String SECRETO = "secreto-de-prueba-con-mas-de-32-caracteres!!";
    private static final Instant AHORA = Instant.parse("2026-11-10T10:00:00Z");

    private static Clock reloj(Instant i) {
        return Clock.fixed(i, ZoneOffset.UTC);
    }

    private JwtUtil jwt() {
        return new JwtUtil(SECRETO, 3600, reloj(AHORA));
    }

    private String tokenAdmin() {
        return jwt().generar(7L, "admin", "Administrador", Set.of("ADMIN"));
    }

    private static String b64(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String firmar(String contenido, String secreto) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(contenido.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Un token recién generado es válido y conserva usuario y roles")
    void tokenValido() throws Exception {
        UsuarioAutenticado u = jwt().validar(tokenAdmin());
        assertEquals(7L, u.getId());
        assertEquals("admin", u.getUsername());
        assertEquals("Administrador", u.getNombre());
        assertEquals(Set.of("ADMIN"), u.getRoles());
        assertEquals(AHORA.getEpochSecond() + 3600, u.getExpiraEpoch());
        assertTrue(u.getJti().length() > 10);
    }

    @Test
    @DisplayName("Un token vencido se rechaza con TOKEN_EXPIRADO")
    void tokenExpirado() {
        String token = tokenAdmin();
        JwtUtil despues = new JwtUtil(SECRETO, 3600, reloj(AHORA.plusSeconds(3601)));
        JwtException e = assertThrows(JwtException.class, () -> despues.validar(token));
        assertEquals(JwtException.TOKEN_EXPIRADO, e.getCodigo());
    }

    @Test
    @DisplayName("Justo en el instante de expiración ya no es válido")
    void tokenExpiraExacto() {
        String token = tokenAdmin();
        JwtUtil limite = new JwtUtil(SECRETO, 3600, reloj(AHORA.plusSeconds(3600)));
        assertThrows(JwtException.class, () -> limite.validar(token));
    }

    @Test
    @DisplayName("Si alguien cambia su rol a ADMIN dentro del token, la firma falla")
    void cargaAlterada() {
        String[] p = jwt().generar(2L, "vet", "Vet", Set.of("VETERINARIO")).split("\\.");
        String cargaFalsa = b64("{\"iss\":\"veterinaria-api\",\"sub\":\"vet\",\"uid\":2,\"nombre\":\"Vet\","
                + "\"roles\":[\"ADMIN\"],\"jti\":\"x\",\"iat\":1,\"exp\":" + (AHORA.getEpochSecond() + 9999) + "}");
        String falso = p[0] + "." + cargaFalsa + "." + p[2];
        JwtException e = assertThrows(JwtException.class, () -> jwt().validar(falso));
        assertEquals(JwtException.TOKEN_INVALIDO, e.getCodigo());
    }

    @Test
    @DisplayName("Una firma modificada se rechaza")
    void firmaAlterada() {
        String token = tokenAdmin();
        String falso = token.substring(0, token.length() - 3) + (token.endsWith("AAA") ? "BBB" : "AAA");
        assertThrows(JwtException.class, () -> jwt().validar(falso));
    }

    @Test
    @DisplayName("Un token firmado con OTRO secreto se rechaza")
    void otroSecreto() {
        String ajeno = new JwtUtil("otro-secreto-distinto-de-mas-de-32-caracteres", 3600, reloj(AHORA))
                .generar(7L, "admin", "Administrador", Set.of("ADMIN"));
        assertThrows(JwtException.class, () -> jwt().validar(ajeno));
    }

    @Test
    @DisplayName("Ataque alg:none (token sin firma) se rechaza")
    void algNone() {
        String cuerpo = b64("{\"alg\":\"none\",\"typ\":\"JWT\"}") + "."
                + b64("{\"iss\":\"veterinaria-api\",\"sub\":\"admin\",\"uid\":1,\"roles\":[\"ADMIN\"],\"exp\":"
                + (AHORA.getEpochSecond() + 3600) + "}");
        assertThrows(JwtException.class, () -> jwt().validar(cuerpo + "."));
        assertThrows(JwtException.class, () -> jwt().validar(cuerpo));
    }

    @Test
    @DisplayName("Un algoritmo distinto de HS256 se rechaza aunque la firma sea correcta")
    void otroAlgoritmo() throws Exception {
        String cuerpo = b64("{\"alg\":\"HS512\",\"typ\":\"JWT\"}") + "."
                + b64("{\"iss\":\"veterinaria-api\",\"sub\":\"admin\",\"uid\":1,\"roles\":[\"ADMIN\"],\"exp\":"
                + (AHORA.getEpochSecond() + 3600) + "}");
        assertThrows(JwtException.class, () -> jwt().validar(cuerpo + "." + firmar(cuerpo, SECRETO)));
    }

    @Test
    @DisplayName("Un emisor desconocido se rechaza aunque la firma sea correcta")
    void emisorDesconocido() throws Exception {
        String cuerpo = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}") + "."
                + b64("{\"iss\":\"otra-app\",\"sub\":\"admin\",\"uid\":1,\"roles\":[\"ADMIN\"],\"exp\":"
                + (AHORA.getEpochSecond() + 3600) + "}");
        assertThrows(JwtException.class, () -> jwt().validar(cuerpo + "." + firmar(cuerpo, SECRETO)));
    }

    @Test
    @DisplayName("Un token sin fecha de expiración se rechaza")
    void sinExp() throws Exception {
        String cuerpo = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}") + "."
                + b64("{\"iss\":\"veterinaria-api\",\"sub\":\"admin\",\"uid\":1,\"roles\":[\"ADMIN\"]}");
        assertThrows(JwtException.class, () -> jwt().validar(cuerpo + "." + firmar(cuerpo, SECRETO)));
    }

    @Test
    @DisplayName("Entradas basura, vacías o nulas se rechazan sin lanzar errores inesperados")
    void basura() {
        for (String t : new String[]{null, "", "   ", "abc", "a.b", "a.b.c", "....", "###.###.###"}) {
            assertThrows(JwtException.class, () -> jwt().validar(t), "debía rechazar: " + t);
        }
    }

    @Test
    @DisplayName("El secreto debe tener al menos 32 caracteres")
    void secretoCorto() {
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil("corto", 60, Clock.systemUTC()));
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil(null, 60, Clock.systemUTC()));
    }

    @Test
    @DisplayName("Cada token tiene un identificador (jti) distinto")
    void jtiUnico() throws Exception {
        String a = jwt().validar(tokenAdmin()).getJti();
        String b = jwt().validar(tokenAdmin()).getJti();
        assertTrue(!a.equals(b));
    }

    @Test
    @DisplayName("extraerBearer lee el encabezado Authorization correctamente")
    void bearer() {
        assertEquals("abc.def.ghi", JwtUtil.extraerBearer("Bearer abc.def.ghi"));
        assertEquals("abc.def.ghi", JwtUtil.extraerBearer("bearer   abc.def.ghi "));
        assertNull(JwtUtil.extraerBearer(null));
        assertNull(JwtUtil.extraerBearer("Basic dXNlcjpwYXNz"));
        assertNull(JwtUtil.extraerBearer("Bearer"));
        assertNull(JwtUtil.extraerBearer(""));
    }

    @Test
    @DisplayName("Un token revocado (logout) queda registrado hasta que expire")
    void revocacion() {
        TokenRevocados.vaciar();
        long futuro = Instant.now().getEpochSecond() + 3600;
        TokenRevocados.revocar("abc-123", futuro);
        assertTrue(TokenRevocados.estaRevocado("abc-123"));
        assertTrue(!TokenRevocados.estaRevocado("otro"));
        TokenRevocados.revocar("viejo", Instant.now().getEpochSecond() - 10);   // ya expirado
        TokenRevocados.revocar("nuevo", futuro);                                 // dispara la limpieza
        assertTrue(!TokenRevocados.estaRevocado("viejo"));
        TokenRevocados.vaciar();
    }
}
