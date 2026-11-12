package com.mycompany.veterinaria.dao;

import com.mycompany.veterinaria.model.entity.Cita;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class CitaDAO extends GenericDAO<Cita, Long> {

    public CitaDAO(EntityManager em) {
        super(em, Cita.class);
    }

    /** Filtros combinables (RF-09). Cualquiera puede ser null. 'hasta' es exclusivo. */
    public List<Cita> buscar(String estado, Long idVeterinario, Long idMascota,
            LocalDateTime desde, LocalDateTime hasta) {
        return em.createQuery(
                "SELECT c FROM Cita c JOIN FETCH c.mascota JOIN FETCH c.veterinario"
                + " WHERE (:estado IS NULL OR c.estado = :estado)"
                + " AND (:vet IS NULL OR c.veterinario.idVeterinario = :vet)"
                + " AND (:masc IS NULL OR c.mascota.idMascota = :masc)"
                + " AND (:desde IS NULL OR c.fechaHora >= :desde)"
                + " AND (:hasta IS NULL OR c.fechaHora < :hasta)"
                + " ORDER BY c.fechaHora", Cita.class)
                .setParameter("estado", estado)
                .setParameter("vet", idVeterinario)
                .setParameter("masc", idMascota)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();
    }

    /**
     * Citas PROGRAMADAS de un veterinario o de una mascota que empiezan en la ventana
     * [desde, hasta). Con ellas el servicio comprueba el solapamiento exacto en Java.
     */
    public List<Cita> programadasEnVentana(Long idVeterinario, Long idMascota,
            LocalDateTime desde, LocalDateTime hasta, Long excluirId) {
        return em.createQuery(
                "SELECT c FROM Cita c WHERE c.estado = 'PROGRAMADA'"
                + " AND (c.veterinario.idVeterinario = :vet OR c.mascota.idMascota = :masc)"
                + " AND c.fechaHora >= :desde AND c.fechaHora < :hasta"
                + " AND (:excluir IS NULL OR c.idCita <> :excluir)", Cita.class)
                .setParameter("vet", idVeterinario)
                .setParameter("masc", idMascota)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .setParameter("excluir", excluirId)
                .getResultList();
    }

    public long contarPorEstado(String estado) {
        return em.createQuery("SELECT COUNT(c) FROM Cita c WHERE c.estado = :e", Long.class)
                .setParameter("e", estado).getSingleResult();
    }
}
