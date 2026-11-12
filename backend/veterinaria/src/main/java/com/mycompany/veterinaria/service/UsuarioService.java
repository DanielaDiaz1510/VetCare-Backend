package com.mycompany.veterinaria.service;

import com.mycompany.veterinaria.DTO.UsuarioRequestDTO;
import com.mycompany.veterinaria.dao.RolDAO;
import com.mycompany.veterinaria.dao.UsuarioDAO;
import com.mycompany.veterinaria.dao.VeterinarioDAO;
import com.mycompany.veterinaria.exception.NoEncontradoException;
import com.mycompany.veterinaria.exception.ReglaNegocioException;
import com.mycompany.veterinaria.exception.ValidacionException;
import com.mycompany.veterinaria.model.entity.Rol;
import com.mycompany.veterinaria.model.entity.Usuario;
import com.mycompany.veterinaria.model.entity.UsuarioRol;
import com.mycompany.veterinaria.model.entity.Veterinario;
import com.mycompany.veterinaria.security.Permisos;
import com.mycompany.veterinaria.util.PasswordUtil;
import com.mycompany.veterinaria.util.Tx;
import com.mycompany.veterinaria.util.Validador;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gestión de usuarios: alta, edición, registro público y perfil (RF-01, RF-03, RF-04).
 * El hash de la contraseña NUNCA sale de este servicio.
 */
public class UsuarioService {

    // ------------------------------------------------------------------ consultas

    public List<Map<String, Object>> listarTodosParaVista() {
        return Tx.leer(em -> new UsuarioDAO(em).listarTodos().stream().map(this::aMapa).toList());
    }

    public Map<String, Object> obtenerPorId(Long id) {
        return Tx.leer(em -> {
            Usuario u = new UsuarioDAO(em).buscarPorId(id);
            if (u == null) {
                throw new NoEncontradoException("Usuario no encontrado");
            }
            return aMapa(u);
        });
    }

    /** Perfil del usuario autenticado (RF-04): sus datos, roles y, si lo tiene, su perfil de veterinario. */
    public Map<String, Object> perfil(Long idUsuario) {
        return Tx.leer(em -> {
            Usuario u = new UsuarioDAO(em).buscarPorId(idUsuario);
            if (u == null) {
                throw new NoEncontradoException("Usuario no encontrado");
            }
            Map<String, Object> m = aMapa(u);
            Veterinario v = new VeterinarioDAO(em).buscarPorUsuario(idUsuario);
            m.put("idVeterinario", v == null ? null : v.getIdVeterinario());
            m.put("nombreProfesional", v == null ? null : v.getNombres() + " " + v.getApellidos());
            return m;
        });
    }

    // ------------------------------------------------------------------ altas y cambios

    /** Alta hecha por un ADMIN: puede elegir roles y estado. */
    public Map<String, Object> crear(UsuarioRequestDTO d, Long idActor) {
        Map<String, Object> creado = Tx.ejecutar(em -> {
            Datos x = validar(d, true);
            comprobarDuplicados(em, x.username, x.email, null);
            Usuario u = new Usuario();
            u.setUsername(x.username);
            u.setNombre(x.nombre);
            u.setEmail(x.email);
            u.setPasswordHash(PasswordUtil.cifrar(x.password));
            u.setEstado(d.getEstado() == null || d.getEstado());
            u.setRoles(new ArrayList<>());
            asignarRoles(em, u, d.getRoles() == null || d.getRoles().isEmpty()
                    ? List.of(Permisos.VETERINARIO) : d.getRoles());
            em.persist(u);
            return aMapa(u);
        });
        new AuditoriaService().registrar(idActor, "CREAR", "Usuario", ((Number) creado.get("idUsuario")).longValue(),
                "Creación de usuario: " + creado.get("username"));
        return creado;
    }

    /** Registro público: SIEMPRE crea un VETERINARIO inactivo, a la espera de que un ADMIN lo apruebe. */
    public Map<String, Object> registrar(UsuarioRequestDTO d) {
        Map<String, Object> creado = Tx.ejecutar(em -> {
            Datos x = validar(d, true);
            comprobarDuplicados(em, x.username, x.email, null);
            Usuario u = new Usuario();
            u.setUsername(x.username);
            u.setNombre(x.nombre);
            u.setEmail(x.email);
            u.setPasswordHash(PasswordUtil.cifrar(x.password));
            u.setEstado(false);                                   // pendiente de aprobación
            u.setRoles(new ArrayList<>());
            asignarRoles(em, u, List.of(Permisos.VETERINARIO));   // nunca ADMIN desde un registro público
            em.persist(u);
            return aMapa(u);
        });
        new AuditoriaService().registrar(((Number) creado.get("idUsuario")).longValue(), "REGISTRO", "Usuario",
                ((Number) creado.get("idUsuario")).longValue(), "Auto-registro pendiente de aprobación: " + creado.get("username"));
        return creado;
    }

    public void actualizar(UsuarioRequestDTO d, Long idActor) {
        if (d.getIdUsuario() == null) {
            throw new ValidacionException("Falta el campo 'idUsuario'");
        }
        Tx.ejecutar(em -> {
            Usuario u = em.find(Usuario.class, d.getIdUsuario());
            if (u == null) {
                throw new NoEncontradoException("Usuario no encontrado");
            }
            Datos x = validar(d, false);
            comprobarDuplicados(em, x.username, x.email, u.getIdUsuario());

            boolean activo = d.getEstado() == null ? Boolean.TRUE.equals(u.getEstado()) : d.getEstado();
            boolean esUnoMismo = u.getIdUsuario().equals(idActor);

            // Regla: un administrador no puede dejarse a sí mismo sin acceso
            if (esUnoMismo && !activo) {
                throw new ReglaNegocioException("No puede desactivar su propia cuenta");
            }
            if (esUnoMismo && d.getRoles() != null && !d.getRoles().stream().anyMatch(r -> Permisos.ADMIN.equalsIgnoreCase(r))) {
                throw new ReglaNegocioException("No puede quitarse a sí mismo el rol ADMIN");
            }

            u.setUsername(x.username);
            u.setNombre(x.nombre);
            u.setEmail(x.email);
            u.setEstado(activo);
            if (x.password != null) {
                u.setPasswordHash(PasswordUtil.cifrar(x.password));   // solo si enviaron una nueva
            }
            if (d.getRoles() != null) {
                if (d.getRoles().isEmpty()) {
                    throw new ValidacionException("El usuario debe tener al menos un rol");
                }
                u.getRoles().clear();                                 // orphanRemoval borra los anteriores
                em.flush();
                asignarRoles(em, u, d.getRoles());
            }
            return null;
        });
        new AuditoriaService().registrar(idActor, "ACTUALIZAR", "Usuario", d.getIdUsuario(),
                "Actualización de usuario: " + d.getUsername());
    }

    // ------------------------------------------------------------------ internos

    private static class Datos {
        String username; String nombre; String email; String password;
    }

    /** Valida y normaliza. En el alta la contraseña es obligatoria; en la edición solo si se envía. */
    private Datos validar(UsuarioRequestDTO d, boolean alta) {
        Datos x = new Datos();
        x.username = Validador.username(d.getUsername());
        x.nombre = Validador.requerido(d.getNombre(), "nombre", 100);
        x.email = Validador.emailRequerido(d.getEmail());
        if (alta || (d.getPassword() != null && !d.getPassword().isBlank())) {
            x.password = Validador.password(d.getPassword());
        }
        return x;
    }

    private void comprobarDuplicados(EntityManager em, String username, String email, Long excluirId) {
        UsuarioDAO dao = new UsuarioDAO(em);
        if (dao.existeUsername(username, excluirId)) {
            throw new ReglaNegocioException("El nombre de usuario '" + username + "' ya está registrado");
        }
        if (dao.existeEmail(email, excluirId)) {
            throw new ReglaNegocioException("El correo '" + email + "' ya está registrado");
        }
    }

    private void asignarRoles(EntityManager em, Usuario u, List<String> nombres) {
        RolDAO rolDAO = new RolDAO(em);
        Set<String> vistos = new LinkedHashSet<>();
        for (String nombre : nombres) {
            Rol rol = rolDAO.buscarPorNombre(Validador.requerido(nombre, "rol", 30));
            if (rol == null) {
                throw new ValidacionException("El rol '" + nombre + "' no existe");
            }
            if (vistos.add(rol.getNombre())) {
                UsuarioRol ur = new UsuarioRol();
                ur.setUsuario(u);
                ur.setRol(rol);
                u.getRoles().add(ur);
            }
        }
    }

    /** Vista segura del usuario: sin hash de contraseña. */
    private Map<String, Object> aMapa(Usuario u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("idUsuario", u.getIdUsuario());
        m.put("username", u.getUsername());
        m.put("nombre", u.getNombre());
        m.put("email", u.getEmail());
        m.put("estado", Boolean.TRUE.equals(u.getEstado()));
        m.put("roles", u.getRoles() == null ? List.of()
                : u.getRoles().stream().map(ur -> ur.getRol().getNombre()).toList());
        return m;
    }
}
