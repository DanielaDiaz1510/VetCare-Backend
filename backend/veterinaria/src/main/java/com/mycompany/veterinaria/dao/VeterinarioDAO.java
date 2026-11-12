
package com.mycompany.veterinaria.dao;

import com.mycompany.veterinaria.model.entity.Veterinario;
import jakarta.persistence.EntityManager;
import java.util.List;

public class VeterinarioDAO extends GenericDAO<Veterinario, Long> {

    public VeterinarioDAO(EntityManager em) {
        super(em, Veterinario.class);
    }
        // Método para contar los veterinarios
    public long count() {
        return em.createQuery("SELECT COUNT(v) FROM Veterinario v", Long.class)
                .getSingleResult();
    }

    /** Perfil profesional ligado a una cuenta de usuario (o null si no tiene). */
    public Veterinario buscarPorUsuario(Long idUsuario) {
        return em.createQuery("SELECT v FROM Veterinario v WHERE v.usuario.idUsuario = :id", Veterinario.class)
                .setParameter("id", idUsuario)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public boolean existeDocumento(String tipo, String numero, Long excluirId) {
        return em.createQuery(
                "SELECT COUNT(v) FROM Veterinario v WHERE v.tipoDocumento = :t AND v.numeroDocumento = :n"
                + " AND (:excluir IS NULL OR v.idVeterinario <> :excluir)", Long.class)
                .setParameter("t", tipo).setParameter("n", numero).setParameter("excluir", excluirId)
                .setFlushMode(jakarta.persistence.FlushModeType.COMMIT)   // no volcar cambios pendientes antes de comprobar
                .getSingleResult() > 0;
    }

    /** Filtro por texto (nombre, apellido o documento) y por estado. Cualquier criterio puede ser null. */
    public List<Veterinario> buscar(String q, String estado) {
        return em.createQuery(
                "SELECT v FROM Veterinario v WHERE (:estado IS NULL OR v.estado = :estado)"
                + " AND (:q IS NULL OR LOWER(CONCAT(v.nombres, ' ', v.apellidos)) LIKE :q"
                + " OR v.numeroDocumento LIKE :q) ORDER BY v.apellidos, v.nombres", Veterinario.class)
                .setParameter("estado", estado)
                .setParameter("q", q == null ? null : "%" + q.toLowerCase() + "%")
                .getResultList();
    }
}
