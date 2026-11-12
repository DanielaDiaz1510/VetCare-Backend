package com.mycompany.veterinaria.security;

import java.util.Set;

/**
 * Datos del usuario que hizo la petición, sacados del token JWT ya verificado.
 * El filtro de seguridad lo guarda en la petición para que los controladores lo usen.
 */
public class UsuarioAutenticado {

    public static final String ATRIBUTO = "usuarioAutenticado";

    private final Long id;
    private final String username;
    private final String nombre;
    private final Set<String> roles;
    private final String jti;          // identificador único del token
    private final long expiraEpoch;    // segundos desde 1970

    public UsuarioAutenticado(Long id, String username, String nombre, Set<String> roles, String jti, long expiraEpoch) {
        this.id = id;
        this.username = username;
        this.nombre = nombre;
        this.roles = roles;
        this.jti = jti;
        this.expiraEpoch = expiraEpoch;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getNombre() { return nombre; }
    public Set<String> getRoles() { return roles; }
    public String getJti() { return jti; }
    public long getExpiraEpoch() { return expiraEpoch; }

    /** Usuario autenticado de la petición actual (lo dejó el filtro de seguridad). Nulo si la ruta es pública. */
    public static UsuarioAutenticado desde(jakarta.servlet.http.HttpServletRequest req) {
        return (UsuarioAutenticado) req.getAttribute(ATRIBUTO);
    }

    public boolean tieneRol(String rol) {
        return roles != null && roles.contains(rol);
    }
}
