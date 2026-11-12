package com.mycompany.veterinaria.service;

import com.mycompany.veterinaria.DTO.UsuarioDTO;
import com.mycompany.veterinaria.dao.UsuarioDAO;
import com.mycompany.veterinaria.exception.AccesoDenegadoException;
import com.mycompany.veterinaria.exception.DemasiadosIntentosException;
import com.mycompany.veterinaria.exception.NoAutenticadoException;
import com.mycompany.veterinaria.exception.ValidacionException;
import com.mycompany.veterinaria.model.entity.Usuario;
import com.mycompany.veterinaria.util.Config;
import com.mycompany.veterinaria.util.PasswordUtil;
import com.mycompany.veterinaria.util.Tx;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Lógica del inicio de sesión (RF-02).
 *
 * Medidas de seguridad:
 *  - Las contraseñas se guardan con BCrypt (hash con sal); nunca se almacenan ni se comparan en texto plano.
 *  - El mensaje de error es el MISMO si el usuario no existe o la contraseña es mala (no revela qué usuarios existen).
 *  - Si el usuario no existe igual se calcula un BCrypt de relleno, para que ambas respuestas tarden parecido.
 *  - Tras 5 intentos fallidos la cuenta queda bloqueada 5 minutos (frena adivinar contraseñas por fuerza bruta).
 */
public class AuthService {

    private static final String HASH_RELLENO = PasswordUtil.cifrar("relleno-para-igualar-tiempos-1");
    private static final long BLOQUEO_MS = Config.getLong("LOGIN_BLOQUEO_SEGUNDOS", 300) * 1000;
    private static final int MAX_INTENTOS = (int) Config.getLong("LOGIN_MAX_INTENTOS", 5);

    /** clave (usuario en minúsculas) -> [fallos acumulados, bloqueado hasta (ms)] */
    private static final Map<String, long[]> FALLOS = new ConcurrentHashMap<>();

    /** Datos mínimos leídos de la BD (para no mantener la entidad fuera de su sesión). */
    private static class Cuenta {
        Long id; String username; String nombre; String email; boolean estado; String hash; Set<String> roles;
    }

    public UsuarioDTO login(String identificador, String password) {
        if (identificador == null || identificador.isBlank() || password == null || password.isEmpty()) {
            throw new ValidacionException("Usuario y contraseña son obligatorios");
        }
        String clave = identificador.trim().toLowerCase();
        verificarBloqueo(clave);

        Cuenta c = Tx.leer(em -> {
            Usuario u = new UsuarioDAO(em).buscarPorIdentificador(identificador);
            if (u == null) {
                return null;
            }
            Cuenta r = new Cuenta();
            r.id = u.getIdUsuario();
            r.username = u.getUsername();
            r.nombre = u.getNombre();
            r.email = u.getEmail();
            r.estado = Boolean.TRUE.equals(u.getEstado());
            r.hash = u.getPasswordHash();
            r.roles = u.getRoles() == null ? Set.of() : u.getRoles().stream()
                    .map(ur -> ur.getRol().getNombre()).collect(Collectors.toSet());
            return r;
        });

        boolean correcto = coincide(password, c == null ? HASH_RELLENO : c.hash) && c != null;
        if (!correcto) {
            registrarFallo(clave);
            throw new NoAutenticadoException("Usuario o contraseña incorrectos");
        }
        FALLOS.remove(clave);

        // La contraseña es correcta, pero la cuenta puede estar desactivada o pendiente de aprobación
        if (!c.estado) {
            throw new AccesoDenegadoException("La cuenta está inactiva o pendiente de aprobación por un administrador");
        }
        return new UsuarioDTO(c.id, c.username, c.nombre, c.email, true, c.roles);
    }

    /** ¿Sigue activa la cuenta? El filtro lo consulta en cada petición para que desactivar un usuario sea inmediato. */
    public boolean cuentaActiva(Long idUsuario) {
        return Tx.leer(em -> {
            Usuario u = new UsuarioDAO(em).buscarPorId(idUsuario);
            return u != null && Boolean.TRUE.equals(u.getEstado());
        });
    }

    private static boolean coincide(String plana, String hash) {
        try {
            return hash != null && PasswordUtil.verificar(plana, hash);
        } catch (IllegalArgumentException e) {
            return false;                                   // hash corrupto en la BD
        }
    }

    // ------------------------------------------------------------- límite de intentos

    private static void verificarBloqueo(String clave) {
        long[] v = FALLOS.get(clave);
        long ahora = System.currentTimeMillis();
        if (v != null && v[1] > ahora) {
            long minutos = Math.max(1, (v[1] - ahora + 59_999) / 60_000);
            throw new DemasiadosIntentosException(
                    "Demasiados intentos fallidos. Intente de nuevo en " + minutos + " minuto(s)");
        }
    }

    private static void registrarFallo(String clave) {
        long ahora = System.currentTimeMillis();
        if (FALLOS.size() > 10_000) {                       // evita crecer sin límite
            FALLOS.values().removeIf(v -> v[1] != 0 && v[1] <= ahora);
        }
        FALLOS.compute(clave, (k, v) -> {
            if (v == null || (v[1] != 0 && v[1] <= ahora)) {
                v = new long[]{0, 0};                       // bloqueo vencido: se empieza de cero
            }
            v[0]++;
            if (v[0] >= MAX_INTENTOS) {
                v[1] = ahora + BLOQUEO_MS;
                v[0] = 0;
            }
            return v;
        });
    }

    /** Solo para pruebas automáticas. */
    public static void reiniciarIntentos() {
        FALLOS.clear();
    }
}
