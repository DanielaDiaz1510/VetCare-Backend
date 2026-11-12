package com.mycompany.veterinaria.dao;

import com.mycompany.veterinaria.model.entity.Rol;
import jakarta.persistence.EntityManager;

public class RolDAO extends GenericDAO<Rol, Long> {

    public RolDAO(EntityManager em) {
        super(em, Rol.class);
    }

    public long count() {
        return em.createQuery("SELECT COUNT(r) FROM Rol r", Long.class).getSingleResult();
    }

    public Rol buscarPorNombre(String nombre) {
        return em.createQuery("SELECT r FROM Rol r WHERE UPPER(r.nombre) = :n", Rol.class)
                .setParameter("n", nombre.trim().toUpperCase())
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}
