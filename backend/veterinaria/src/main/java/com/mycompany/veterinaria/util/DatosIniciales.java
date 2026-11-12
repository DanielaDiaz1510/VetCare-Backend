package com.mycompany.veterinaria.util;

import com.mycompany.veterinaria.model.entity.Rol;
import com.mycompany.veterinaria.model.entity.Usuario;
import com.mycompany.veterinaria.model.entity.UsuarioRol;
import com.mycompany.veterinaria.model.entity.Veterinario;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.ArrayList;

/**
 * Se ejecuta una vez cuando arranca la aplicación y deja lista la base de datos mínima:
 *   - los roles ADMIN y VETERINARIO (si no existen);
 *   - si NO hay ningún usuario: el administrador  admin / admin123
 *     y un veterinario de demostración           veterinario / vet12345  (con su perfil profesional).
 *
 * IMPORTANTE: son credenciales de DEMOSTRACIÓN. Cámbielas antes de usar el sistema con datos reales.
 * Es seguro ejecutarlo varias veces: solo crea lo que falta.
 */
@WebListener
public class DatosIniciales implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Tx.ejecutar(em -> {
                Rol admin = asegurarRol(em, "ADMIN", "Administrador del sistema");
                Rol vet = asegurarRol(em, "VETERINARIO", "Veterinario: gestiona pacientes, citas e historia clínica");

                Long usuarios = em.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class).getSingleResult();
                if (usuarios == 0) {
                    crearUsuario(em, "admin", "admin123", "Administrador", "admin@veterinaria.com", admin);
                    Usuario uVet = crearUsuario(em, "veterinario", "vet12345", "Veterinario Demo", "veterinario@veterinaria.com", vet);

                    Veterinario perfil = new Veterinario();
                    perfil.setTipoDocumento("CC");
                    perfil.setNumeroDocumento("1000000001");
                    perfil.setNombres("Veterinario");
                    perfil.setApellidos("Demo");
                    perfil.setEspecialidad("Medicina general");
                    perfil.setEstado("ACTIVO");
                    perfil.setUsuario(uVet);
                    em.persist(perfil);
                    System.out.println(">>> Usuarios iniciales creados: admin / admin123  y  veterinario / vet12345");
                }
                return null;
            });
        } catch (Exception e) {
            System.err.println(">>> No se pudieron crear los datos iniciales: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JPAUtil.cerrar();
    }

    private Rol asegurarRol(EntityManager em, String nombre, String descripcion) {
        return em.createQuery("SELECT r FROM Rol r WHERE r.nombre = :n", Rol.class)
                .setParameter("n", nombre).getResultStream().findFirst().orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre(nombre);
            r.setDescripcion(descripcion);
            em.persist(r);
            return r;
        });
    }

    private Usuario crearUsuario(EntityManager em, String username, String password, String nombre, String email, Rol rol) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPasswordHash(PasswordUtil.cifrar(password));
        u.setNombre(nombre);
        u.setEmail(email);
        u.setEstado(true);
        u.setRoles(new ArrayList<>());
        UsuarioRol ur = new UsuarioRol();
        ur.setUsuario(u);
        ur.setRol(rol);
        u.getRoles().add(ur);
        em.persist(u);
        return u;
    }
}
