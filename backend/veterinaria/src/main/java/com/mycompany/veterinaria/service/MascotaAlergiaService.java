package com.mycompany.veterinaria.service;

import com.mycompany.veterinaria.DTO.MascotaAlergiaDTO;
import com.mycompany.veterinaria.exception.NoEncontradoException;
import com.mycompany.veterinaria.exception.ReglaNegocioException;
import com.mycompany.veterinaria.exception.ValidacionException;
import com.mycompany.veterinaria.model.entity.Alergia;
import com.mycompany.veterinaria.model.entity.Mascota;
import com.mycompany.veterinaria.model.entity.MascotaAlergia;
import com.mycompany.veterinaria.util.Tx;
import com.mycompany.veterinaria.util.Validador;
import java.util.List;

/** Alergias conocidas de cada mascota (antecedentes). Relación muchos a muchos Mascota - Alergia. */
public class MascotaAlergiaService {

    private final AuditoriaService auditoria = new AuditoriaService();

    public List<MascotaAlergiaDTO> listarPorMascota(Long idMascota) {
        return Tx.leer(em -> em.createQuery(
                "SELECT ma FROM MascotaAlergia ma WHERE ma.mascota.idMascota = :m ORDER BY ma.alergia.nombre",
                MascotaAlergia.class).setParameter("m", idMascota).getResultList()
                .stream().map(MascotaAlergiaService::aDTO).toList());
    }

    public MascotaAlergiaDTO crear(MascotaAlergiaDTO d, Long idActor) {
        MascotaAlergiaDTO creada = Tx.ejecutar(em -> {
            if (d.getIdMascota() == null || d.getIdAlergia() == null) {
                throw new ValidacionException("Faltan los campos 'idMascota' e 'idAlergia'");
            }
            Mascota m = em.find(Mascota.class, d.getIdMascota());
            Alergia a = em.find(Alergia.class, d.getIdAlergia());
            if (m == null) {
                throw new NoEncontradoException("Mascota no encontrada");
            }
            if (a == null) {
                throw new NoEncontradoException("Alergia no encontrada");
            }
            long ya = em.createQuery("SELECT COUNT(ma) FROM MascotaAlergia ma WHERE ma.mascota.idMascota = :m AND ma.alergia.idAlergia = :a",
                    Long.class).setParameter("m", m.getIdMascota()).setParameter("a", a.getIdAlergia()).getSingleResult();
            if (ya > 0) {
                throw new ReglaNegocioException(m.getNombre() + " ya tiene registrada la alergia '" + a.getNombre() + "'");
            }
            MascotaAlergia ma = new MascotaAlergia();
            ma.setMascota(m);
            ma.setAlergia(a);
            ma.setObservaciones(Validador.opcional(d.getObservaciones(), "observaciones", 255));
            em.persist(ma);
            return aDTO(ma);
        });
        auditoria.registrar(idActor, "CREAR", "MascotaAlergia", creada.getId(),
                "Alergia " + creada.getNombreAlergia() + " registrada a " + creada.getNombreMascota());
        return creada;
    }

    public void eliminar(Long id, Long idActor) {
        Tx.ejecutar(em -> {
            MascotaAlergia ma = id == null ? null : em.find(MascotaAlergia.class, id);
            if (ma == null) {
                throw new NoEncontradoException("Registro de alergia no encontrado");
            }
            em.remove(ma);
            return null;
        });
        auditoria.registrar(idActor, "ELIMINAR", "MascotaAlergia", id, "Eliminación de alergia de mascota");
    }

    static MascotaAlergiaDTO aDTO(MascotaAlergia ma) {
        MascotaAlergiaDTO d = new MascotaAlergiaDTO();
        d.setId(ma.getId());
        d.setObservaciones(ma.getObservaciones());
        d.setIdMascota(ma.getMascota().getIdMascota());
        d.setNombreMascota(ma.getMascota().getNombre());
        d.setIdAlergia(ma.getAlergia().getIdAlergia());
        d.setNombreAlergia(ma.getAlergia().getNombre());
        return d;
    }
}
