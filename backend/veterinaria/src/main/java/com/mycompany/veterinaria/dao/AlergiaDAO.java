
package com.mycompany.veterinaria.dao;

import com.mycompany.veterinaria.model.entity.Alergia;
import jakarta.persistence.EntityManager;

public class AlergiaDAO extends GenericDAO<Alergia, Long> {

    public AlergiaDAO(EntityManager em) {
        super(em, Alergia.class);
    }
}