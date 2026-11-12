
package com.mycompany.veterinaria.dao;

import com.mycompany.veterinaria.model.entity.Tratamiento;
import jakarta.persistence.EntityManager;
import java.util.List;

public class TratamientoDAO extends GenericDAO<Tratamiento, Long> {

    public TratamientoDAO(EntityManager em) {
        super(em, Tratamiento.class);
    }

    public List<Tratamiento> buscarPorHistorial(Long idHistorial) {
        return em.createQuery(
                "SELECT t FROM Tratamiento t WHERE t.historial.idHistorial = :id",
                Tratamiento.class
        ).setParameter("id", idHistorial)
         .getResultList();
    }
}