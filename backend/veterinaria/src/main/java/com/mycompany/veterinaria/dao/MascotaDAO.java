
package com.mycompany.veterinaria.dao;

import com.mycompany.veterinaria.model.entity.Mascota;
import jakarta.persistence.EntityManager;
import java.util.List;

public class MascotaDAO extends GenericDAO<Mascota, Long> {

    public MascotaDAO(EntityManager em) {
        super(em, Mascota.class);
    }

    public List<Mascota> buscarPorPropietario(Long idPropietario) {
        return em.createQuery(
                "SELECT m FROM Mascota m WHERE m.propietario.idPropietario = :id",
                Mascota.class
        ).setParameter("id", idPropietario)
         .getResultList();
    }
    // Método para contar las mascotas
    public long count() {
        return em.createQuery("SELECT COUNT(m) FROM Mascota m", Long.class)
                .getSingleResult();
    }

    /** Búsqueda por varios criterios (RF-09): nombre, especie, estado y propietario. Todos opcionales. */
    public List<Mascota> buscar(String q, String especie, String estado, Long idPropietario) {
        return em.createQuery(
                "SELECT m FROM Mascota m WHERE (:q IS NULL OR LOWER(m.nombre) LIKE :q)"
                + " AND (:especie IS NULL OR LOWER(m.especie) = :especie)"
                + " AND (:estado IS NULL OR m.estado = :estado)"
                + " AND (:prop IS NULL OR m.propietario.idPropietario = :prop)"
                + " ORDER BY m.nombre", Mascota.class)
                .setParameter("q", q == null ? null : "%" + q.toLowerCase() + "%")
                .setParameter("especie", especie == null ? null : especie.toLowerCase())
                .setParameter("estado", estado)
                .setParameter("prop", idPropietario)
                .getResultList();
    }
}
