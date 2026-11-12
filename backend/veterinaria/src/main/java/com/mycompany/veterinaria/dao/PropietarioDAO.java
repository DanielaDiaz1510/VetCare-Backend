
package com.mycompany.veterinaria.dao;

import com.mycompany.veterinaria.model.entity.Propietario;
import jakarta.persistence.EntityManager;
import java.util.List;

public class PropietarioDAO extends GenericDAO<Propietario, Long> {

    public PropietarioDAO(EntityManager em) {
        super(em, Propietario.class);
    }
        // Método para contar los propietarios
    public long count() {
        return em.createQuery("SELECT COUNT(p) FROM Propietario p", Long.class)
                .getSingleResult();
    }

    public boolean existeDocumento(String tipo, String numero, Long excluirId) {
        return em.createQuery(
                "SELECT COUNT(p) FROM Propietario p WHERE p.tipoDocumento = :t AND p.numeroDocumento = :n"
                + " AND (:excluir IS NULL OR p.idPropietario <> :excluir)", Long.class)
                .setParameter("t", tipo).setParameter("n", numero).setParameter("excluir", excluirId)
                .setFlushMode(jakarta.persistence.FlushModeType.COMMIT)   // no volcar cambios pendientes antes de comprobar
                .getSingleResult() > 0;
    }

    public long contarMascotas(Long idPropietario) {
        return em.createQuery("SELECT COUNT(m) FROM Mascota m WHERE m.propietario.idPropietario = :id", Long.class)
                .setParameter("id", idPropietario).getSingleResult();
    }

    /** Búsqueda por 2 criterios: texto (nombre/apellido) y documento. Ambos opcionales. */
    public List<Propietario> buscar(String q, String documento) {
        return em.createQuery(
                "SELECT p FROM Propietario p WHERE (:doc IS NULL OR p.numeroDocumento LIKE :doc)"
                + " AND (:q IS NULL OR LOWER(CONCAT(p.nombres, ' ', p.apellidos)) LIKE :q)"
                + " ORDER BY p.apellidos, p.nombres", Propietario.class)
                .setParameter("doc", documento == null ? null : "%" + documento + "%")
                .setParameter("q", q == null ? null : "%" + q.toLowerCase() + "%")
                .getResultList();
    }
}
