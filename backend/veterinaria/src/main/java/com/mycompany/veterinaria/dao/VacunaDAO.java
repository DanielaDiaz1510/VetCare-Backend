
package com.mycompany.veterinaria.dao;

import com.mycompany.veterinaria.model.entity.Vacuna;
import jakarta.persistence.EntityManager;

public class VacunaDAO extends GenericDAO<Vacuna, Long> {

    public VacunaDAO(EntityManager em) {
        super(em, Vacuna.class);
    }
}