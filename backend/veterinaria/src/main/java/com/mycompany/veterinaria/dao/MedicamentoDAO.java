
package com.mycompany.veterinaria.dao;

import com.mycompany.veterinaria.model.entity.Medicamento;
import jakarta.persistence.EntityManager;

public class MedicamentoDAO extends GenericDAO<Medicamento, Long> {

    public MedicamentoDAO(EntityManager em) {
        super(em, Medicamento.class);
    }
}