package com.mycompany.veterinaria.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.veterinaria.util.Config;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Genera y valida tokens JWT firmados con HS256 (HMAC-SHA256).
 *
 * Un JWT son tres partes en Base64URL separadas por puntos:
 *     CABECERA . CARGA(claims) . FIRMA
 *  - Cabecera: {"alg":"HS256","typ":"JWT"}
 *  - Carga:    sub (usuario), uid, nombre, roles, jti (id del token), iss, iat, exp
 *  - Firma:    HMAC-SHA256(cabecera.carga, SECRETO)
 *
 * El servidor NO guarda sesiones: para saber si un token es bueno vuelve a calcular la
 * firma con el secreto. Si alguien cambia un solo carácter (por ejemplo su rol),
 * la firma ya no coincide y el token se rechaza.
 */
public class JwtUtil {

    public static final String EMISOR = "veterinaria-api";
    private static final String SECRETO_DESARROLLO = "clave-solo-para-desarrollo-cambiar-en-produccion-32+";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64D = Base64.getUrlDecoder();

    private static JwtUtil instancia;

    private final byte[] secreto;
    private final long duracionSegundos;
    private final Clock reloj;

    public JwtUtil(String secreto, long duracionSegundos, Clock reloj) {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("El secreto JWT debe tener al menos 32 caracteres");
        }
        this.secreto = secreto.getBytes(StandardCharsets.UTF_8);
        this.duracionSegundos = duracionSegundos;
        this.reloj = reloj;
    }

    /** Instancia compartida, configurada con JWT_SECRET y JWT_EXPIRA_MINUTOS. */
    public static synchronized JwtUtil instancia() {
        if (instancia == null) {
            String secreto = Config.get("JWT_SECRET", null);
            if (secreto == null) {
                System.err.println("[SEGURIDAD] JWT_SECRET no está definido: se usa un secreto de DESARROLLO. "
                        + "Defina la variable de entorno JWT_SECRET antes de usar el sistema en producción.");
                secreto = SECRETO_DESARROLLO;
            }
            long minutos = Config.getLong("JWT_EXPIRA_MINUTOS", 60);
            instancia = new JwtUtil(secreto, minutos * 60, Clock.systemUTC());
        }
        return instancia;
    }

    public long getDuracionSegundos() {
        return duracionSegundos;
    }

    // ------------------------------------------------------------------ generar

    public String generar(Long idUsuario, String username, String nombre, Set<String> roles) {
        try {
            long ahora = reloj.instant().getEpochSecond();

            Map<String, Object> cabecera = new LinkedHashMap<>();
            cabecera.put("alg", "HS256");
            cabecera.put("typ", "JWT");

            Map<String, Object> carga = new LinkedHashMap<>();
            carga.put("iss", EMISOR);
            carga.put("sub", username);
            carga.put("uid", idUsuario);
            carga.put("nombre", nombre);
            carga.put("roles", roles);
            carga.put("jti", UUID.randomUUID().toString());
            carga.put("iat", ahora);
            carga.put("exp", ahora + duracionSegundos);

            String cuerpo = B64.encodeToString(MAPPER.writeValueAsBytes(cabecera))
                    + "." + B64.encodeToString(MAPPER.writeValueAsBytes(carga));
            return cuerpo + "." + B64.encodeToString(firmar(cuerpo));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el token", e);
        }
    }

    // ------------------------------------------------------------------ validar

    public UsuarioAutenticado validar(String token) throws JwtException {
        if (token == null || token.isBlank()) {
            throw invalido("Token ausente");
        }
        String[] partes = token.split("\\.", -1);
        if (partes.length != 3) {
            throw invalido("Formato de token incorrecto");
        }

        try {
            // 1) La cabecera debe declarar EXACTAMENTE HS256 (evita el ataque "alg: none")
            JsonNode cabecera = MAPPER.readTree(B64D.decode(partes[0]));
            if (!"HS256".equals(cabecera.path("alg").asText())) {
                throw invalido("Algoritmo no permitido");
            }

            // 2) La firma debe coincidir. Se compara en tiempo constante para no filtrar información.
            byte[] esperada = firmar(partes[0] + "." + partes[1]);
            byte[] recibida = B64D.decode(partes[2]);
            if (!MessageDigest.isEqual(esperada, recibida)) {
                throw invalido("Firma inválida");
            }

            // 3) Solo ahora, con la firma comprobada, se confía en el contenido
            JsonNode c = MAPPER.readTree(B64D.decode(partes[1]));
            if (!EMISOR.equals(c.path("iss").asText())) {
                throw invalido("Emisor desconocido");
            }
            if (!c.hasNonNull("exp") || !c.hasNonNull("uid") || !c.hasNonNull("sub")) {
                throw invalido("Token incompleto");
            }
            long exp = c.get("exp").asLong();
            if (reloj.instant().getEpochSecond() >= exp) {
                throw new JwtException(JwtException.TOKEN_EXPIRADO, "El token ha expirado");
            }

            Set<String> roles = new LinkedHashSet<>();
            for (JsonNode r : c.path("roles")) {
                roles.add(r.asText());
            }
            return new UsuarioAutenticado(c.get("uid").asLong(), c.get("sub").asText(),
                    c.path("nombre").asText(), roles, c.path("jti").asText(), exp);

        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            throw invalido("Token ilegible");
        }
    }

    // ------------------------------------------------------------------ utilidades

    private byte[] firmar(String contenido) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secreto, "HmacSHA256"));
        return mac.doFinal(contenido.getBytes(StandardCharsets.UTF_8));
    }

    private static JwtException invalido(String detalle) {
        return new JwtException(JwtException.TOKEN_INVALIDO, detalle);
    }

    /** Extrae el token del encabezado "Authorization: Bearer xxx". Devuelve null si no hay. */
    public static String extraerBearer(String encabezado) {
        if (encabezado == null) {
            return null;
        }
        String h = encabezado.trim();
        if (h.length() > 7 && h.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return h.substring(7).trim();
        }
        return null;
    }
}
