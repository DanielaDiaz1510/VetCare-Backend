package com.mycompany.veterinaria.service;

import com.mycompany.veterinaria.dao.RolDAO;
import com.mycompany.veterinaria.exception.NoEncontradoException;
import com.mycompany.veterinaria.exception.ReglaNegocioException;
import com.mycompany.veterinaria.exception.ValidacionException;
import com.mycompany.veterinaria.model.entity.Rol;
import com.mycompany.veterinaria.security.Permisos;
import com.mycompany.veterinaria.util.Tx;
import com.mycompany.veterinaria.util.Validador;
import java.util.List;

public class RolService {

    public List<Rol> listarTodos() {
        return Tx.leer(em -> new RolDAO(em).listarTodos());
    }

    public void crear(Rol rol) {
        String nombre = nombreValido(rol.getNombre());
        Tx.ejecutar(em -> {
            RolDAO dao = new RolDAO(em);
            if (dao.buscarPorNombre(nombre) != null) {
                throw new ReglaNegocioException("Ya existe el rol '" + nombre + "'");
            }
            Rol nuevo = new Rol();
            nuevo.setNombre(nombre);
            nuevo.setDescripcion(Validador.opcional(rol.getDescripcion(), "descripcion", 150));
            em.persist(nuevo);
            return null;
        });
    }

    public void actualizar(Rol datos) {
        String nombre = nombreValido(datos.getNombre());
        Tx.ejecutar(em -> {
            Rol actual = em.find(Rol.class, datos.getIdRol());
            if (actual == null) {
                throw new NoEncontradoException("Rol no encontrado");
            }
            // Los permisos del sistema dependen de estos dos nombres: no se pueden renombrar
            boolean delSistema = Permisos.ADMIN.equals(actual.getNombre()) || Permisos.VETERINARIO.equals(actual.getNombre());
            if (delSistema && !actual.getNombre().equals(nombre)) {
                throw new ReglaNegocioException("El rol " + actual.getNombre() + " es del sistema y no se puede renombrar");
            }
            Rol otro = new RolDAO(em).buscarPorNombre(nombre);
            if (otro != null && !otro.getIdRol().equals(actual.getIdRol())) {
                throw new ReglaNegocioException("Ya existe el rol '" + nombre + "'");
            }
            actual.setNombre(nombre);
            actual.setDescripcion(Validador.opcional(datos.getDescripcion(), "descripcion", 150));
            return null;
        });
    }

    private String nombreValido(String nombre) {
        String n = Validador.requerido(nombre, "nombre", 30).toUpperCase();
        if (!n.matches("[A-Z_]{3,30}")) {
            throw new ValidacionException("El nombre del rol solo puede tener letras y guion bajo (3 a 30 caracteres)");
        }
        return n;
    }
}
