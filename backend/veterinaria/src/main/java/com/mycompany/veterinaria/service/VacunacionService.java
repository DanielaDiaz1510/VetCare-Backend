package com.mycompany.veterinaria.service;

import com.mycompany.veterinaria.DTO.VacunacionDTO;
import com.mycompany.veterinaria.exception.NoEncontradoException;
import com.mycompany.veterinaria.exception.ReglaNegocioException;
import com.mycompany.veterinaria.exception.ValidacionException;
import com.mycompany.veterinaria.model.entity.Mascota;
import com.mycompany.veterinaria.model.entity.Vacuna;
import com.mycompany.veterinaria.model.entity.Vacunacion;
import com.mycompany.veterinaria.util.Tx;
import com.mycompany.veterinaria.util.Validador;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

/** Carnet de vacunación: cada registro une una mascota, una vacuna, la fecha de aplicación y el próximo refuerzo. */
public class VacunacionService {

    private final AuditoriaService auditoria = new AuditoriaService();

    /**
     * Lista vacunaciones. Filtros opcionales:
     *   idMascota     solo las de esa mascota
     *   proximasDias  solo las que tienen refuerzo en los próximos N días (alerta para la pantalla Home)
     */
    public List<VacunacionDTO> listar(Long idMascota, Integer proximasDias) {
        LocalDate hoy = LocalDate.now();
        LocalDate desde = proximasDias == null ? null : hoy;
        LocalDate hasta = proximasDias == null ? null : hoy.plusDays(proximasDias);
        return Tx.leer(em -> em.createQuery(
                "SELECT v FROM Vacunacion v WHERE (:m IS NULL OR v.mascota.idMascota = :m)"
                + " AND (:desde IS NULL OR (v.fechaProxima IS NOT NULL AND v.fechaProxima >= :desde AND v.fechaProxima <= :hasta))"
                + " ORDER BY v.fechaAplicacion DESC", Vacunacion.class)
                .setParameter("m", idMascota).setParameter("desde", desde).setParameter("hasta", hasta)
                .getResultList().stream().map(VacunacionService::aDTO).toList());
    }

    public VacunacionDTO obtener(Long id) {
        return Tx.leer(em -> aDTO(existente(em, id)));
    }

    public VacunacionDTO crear(VacunacionDTO d, Long idActor) {
        VacunacionDTO creada = Tx.ejecutar(em -> {
            Vacunacion v = new Vacunacion();
            if (d.getIdMascota() == null) {
                throw new ValidacionException("Falta el campo 'idMascota'");
            }
            Mascota m = em.find(Mascota.class, d.getIdMascota());
            if (m == null) {
                throw new NoEncontradoException("Mascota no encontrada");
            }
            if (!"ACTIVO".equals(m.getEstado())) {
                throw new ReglaNegocioException("La mascota está inactiva: no admite nuevas vacunaciones");
            }
            v.setMascota(m);
            copiar(em, d, v);
            em.persist(v);
            return aDTO(v);
        });
        auditoria.registrar(idActor, "CREAR", "Vacunacion", creada.getIdVacunacion(),
                "Vacuna " + creada.getNombreVacuna() + " aplicada a " + creada.getNombreMascota());
        return creada;
    }

    public VacunacionDTO actualizar(VacunacionDTO d, Long idActor) {
        VacunacionDTO actualizada = Tx.ejecutar(em -> {
            Vacunacion v = existente(em, d.getIdVacunacion());
            copiar(em, d, v);                                 // la mascota no cambia
            return aDTO(v);
        });
        auditoria.registrar(idActor, "ACTUALIZAR", "Vacunacion", actualizada.getIdVacunacion(),
                "Actualización de vacunación de " + actualizada.getNombreMascota());
        return actualizada;
    }

    public void eliminar(Long id, Long idActor) {
        Tx.ejecutar(em -> {
            em.remove(existente(em, id));
            return null;
        });
        auditoria.registrar(idActor, "ELIMINAR", "Vacunacion", id, "Eliminación de vacunación");
    }

    private Vacunacion existente(EntityManager em, Long id) {
        Vacunacion v = id == null ? null : em.find(Vacunacion.class, id);
        if (v == null) {
            throw new NoEncontradoException("Vacunación no encontrada");
        }
        return v;
    }

    private void copiar(EntityManager em, VacunacionDTO d, Vacunacion v) {
        if (d.getIdVacuna() == null) {
            throw new ValidacionException("Falta el campo 'idVacuna'");
        }
        Vacuna vacuna = em.find(Vacuna.class, d.getIdVacuna());
        if (vacuna == null) {
            throw new NoEncontradoException("Vacuna no encontrada");
        }
        v.setVacuna(vacuna);
        if (d.getFechaAplicacion() == null) {
            throw new ValidacionException("Falta el campo 'fechaAplicacion' (formato AAAA-MM-DD)");
        }
        if (d.getFechaAplicacion().isAfter(LocalDate.now())) {
            throw new ValidacionException("La fecha de aplicación no puede estar en el futuro");
        }
        if (d.getFechaProxima() != null && d.getFechaProxima().isBefore(d.getFechaAplicacion())) {
            throw new ValidacionException("La fecha del próximo refuerzo no puede ser anterior a la aplicación");
        }
        v.setFechaAplicacion(d.getFechaAplicacion());
        v.setFechaProxima(d.getFechaProxima());
        v.setObservaciones(Validador.opcional(d.getObservaciones(), "observaciones", 255));
    }

    static VacunacionDTO aDTO(Vacunacion v) {
        VacunacionDTO d = new VacunacionDTO();
        d.setIdVacunacion(v.getIdVacunacion());
        d.setFechaAplicacion(v.getFechaAplicacion());
        d.setFechaProxima(v.getFechaProxima());
        d.setObservaciones(v.getObservaciones());
        d.setIdMascota(v.getMascota().getIdMascota());
        d.setNombreMascota(v.getMascota().getNombre());
        d.setIdVacuna(v.getVacuna().getIdVacuna());
        d.setNombreVacuna(v.getVacuna().getNombre());
        return d;
    }
}
