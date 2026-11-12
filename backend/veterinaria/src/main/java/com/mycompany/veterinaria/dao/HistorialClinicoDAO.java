/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.veterinaria.dao;

import com.mycompany.veterinaria.model.entity.HistorialClinico;
import jakarta.persistence.EntityManager;
import java.util.List;

public class HistorialClinicoDAO extends GenericDAO<HistorialClinico, Long> {

    public HistorialClinicoDAO(EntityManager em) {
        super(em, HistorialClinico.class);
    }

    public List<HistorialClinico> buscarPorMascota(Long idMascota) {
        return em.createQuery(
                "SELECT h FROM HistorialClinico h WHERE h.mascota.idMascota = :id",
                HistorialClinico.class
        ).setParameter("id", idMascota)
         .getResultList();
    }
}