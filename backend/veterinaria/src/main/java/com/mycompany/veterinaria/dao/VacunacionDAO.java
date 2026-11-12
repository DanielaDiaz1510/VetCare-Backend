
package com.mycompany.veterinaria.dao;

import com.mycompany.veterinaria.model.entity.Vacunacion;
import jakarta.persistence.EntityManager;
import java.util.List;

public class VacunacionDAO extends GenericDAO<Vacunacion, Long> {

    public VacunacionDAO(EntityManager em) {
        super(em, Vacunacion.class);
    }

    public List<Vacunacion> buscarPorMascota(Long idMascota) {
        return em.createQuery(
                "SELECT v FROM Vacunacion v WHERE v.mascota.idMascota = :id",
                Vacunacion.class
        ).setParameter("id", idMascota)
         .getResultList();
    }
}