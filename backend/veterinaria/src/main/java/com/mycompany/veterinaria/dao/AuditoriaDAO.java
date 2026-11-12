package com.mycompany.veterinaria.dao;

import com.mycompany.veterinaria.model.entity.Auditoria;
import jakarta.persistence.EntityManager;
import java.util.List;

public class AuditoriaDAO extends GenericDAO<Auditoria, Long> {

    public AuditoriaDAO(EntityManager em) {
        super(em, Auditoria.class);
    }

    public List<Auditoria> listarPorUsuario(Long idUsuario) {
        return em.createQuery(
                "SELECT a FROM Auditoria a " +
                "WHERE a.usuario.idUsuario = :idUsuario " +
                "ORDER BY a.fechaHora DESC",
                Auditoria.class
        )
        .setParameter("idUsuario", idUsuario)
        .getResultList();
    }

    public List<Auditoria> listarPorEntidad(String entidad) {
        return em.createQuery(
                "SELECT a FROM Auditoria a " +
                "WHERE a.entidad = :entidad " +
                "ORDER BY a.fechaHora DESC",
                Auditoria.class
        )
        .setParameter("entidad", entidad)
        .getResultList();
    }

    // JOIN FETCH trae el usuario junto con la auditoría (el usuario es LAZY)
    public List<Auditoria> listarTodosConUsuario() {
        return em.createQuery(
                "SELECT a FROM Auditoria a " +
                "JOIN FETCH a.usuario " +
                "ORDER BY a.fechaHora DESC",
                Auditoria.class
        ).getResultList();
    }
}
