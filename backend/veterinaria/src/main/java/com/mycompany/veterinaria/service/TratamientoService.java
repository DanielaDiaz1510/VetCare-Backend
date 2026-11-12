package com.mycompany.veterinaria.service;

import com.mycompany.veterinaria.DTO.TratamientoDTO;
import com.mycompany.veterinaria.exception.NoEncontradoException;
import com.mycompany.veterinaria.exception.ValidacionException;
import com.mycompany.veterinaria.model.entity.HistorialClinico;
import com.mycompany.veterinaria.model.entity.Medicamento;
import com.mycompany.veterinaria.model.entity.Tratamiento;
import com.mycompany.veterinaria.security.UsuarioAutenticado;
import com.mycompany.veterinaria.util.Tx;
import com.mycompany.veterinaria.util.Validador;
import jakarta.persistence.EntityManager;
import java.util.List;

/** Medicamentos recetados dentro de una atención clínica. Solo el veterinario de la atención (o un ADMIN) los modifica. */
public class TratamientoService {

    private final AuditoriaService auditoria = new AuditoriaService();

    public List<TratamientoDTO> listarPorHistorial(Long idHistorial) {
        return Tx.leer(em -> {
            HistorialClinicoService.existente(em, idHistorial);
            return em.createQuery("SELECT t FROM Tratamiento t WHERE t.historial.idHistorial = :id ORDER BY t.idTratamiento",
                    Tratamiento.class).setParameter("id", idHistorial).getResultList()
                    .stream().map(TratamientoService::aDTO).toList();
        });
    }

    public TratamientoDTO obtener(Long id) {
        return Tx.leer(em -> aDTO(existente(em, id)));
    }

    public TratamientoDTO crear(TratamientoDTO d, UsuarioAutenticado actor) {
        TratamientoDTO creado = Tx.ejecutar(em -> {
            HistorialClinico h = HistorialClinicoService.existente(em, d.getIdHistorial());
            HistorialClinicoService.comprobarPropiedad(em, h, actor);
            Tratamiento t = new Tratamiento();
            t.setHistorial(h);
            copiar(em, d, t);
            em.persist(t);
            return aDTO(t);
        });
        auditoria.registrar(actor.getId(), "CREAR", "Tratamiento", creado.getIdTratamiento(),
                "Tratamiento " + creado.getNombreMedicamento() + " en historia " + creado.getIdHistorial());
        return creado;
    }

    public TratamientoDTO actualizar(TratamientoDTO d, UsuarioAutenticado actor) {
        TratamientoDTO actualizado = Tx.ejecutar(em -> {
            Tratamiento t = existente(em, d.getIdTratamiento());
            HistorialClinicoService.comprobarPropiedad(em, t.getHistorial(), actor);
            copiar(em, d, t);
            return aDTO(t);
        });
        auditoria.registrar(actor.getId(), "ACTUALIZAR", "Tratamiento", actualizado.getIdTratamiento(),
                "Actualización de tratamiento " + actualizado.getNombreMedicamento());
        return actualizado;
    }

    public void eliminar(Long id, Long idActor) {
        Tx.ejecutar(em -> {
            em.remove(existente(em, id));
            return null;
        });
        auditoria.registrar(idActor, "ELIMINAR", "Tratamiento", id, "Eliminación de tratamiento");
    }

    private Tratamiento existente(EntityManager em, Long id) {
        Tratamiento t = id == null ? null : em.find(Tratamiento.class, id);
        if (t == null) {
            throw new NoEncontradoException("Tratamiento no encontrado");
        }
        return t;
    }

    private void copiar(EntityManager em, TratamientoDTO d, Tratamiento t) {
        if (d.getIdMedicamento() == null) {
            throw new ValidacionException("Falta el campo 'idMedicamento'");
        }
        Medicamento m = em.find(Medicamento.class, d.getIdMedicamento());
        if (m == null) {
            throw new NoEncontradoException("Medicamento no encontrado");
        }
        t.setMedicamento(m);
        t.setDosis(Validador.requerido(d.getDosis(), "dosis", 100));
        t.setFrecuencia(Validador.requerido(d.getFrecuencia(), "frecuencia", 100));
        if (d.getDuracion() != null && (d.getDuracion() < 1 || d.getDuracion() > 365)) {
            throw new ValidacionException("La duración del tratamiento debe estar entre 1 y 365 días");
        }
        t.setDuracion(d.getDuracion());
        t.setIndicaciones(Validador.opcional(d.getIndicaciones(), "indicaciones", 500));
    }

    static TratamientoDTO aDTO(Tratamiento t) {
        TratamientoDTO d = new TratamientoDTO();
        d.setIdTratamiento(t.getIdTratamiento());
        d.setDosis(t.getDosis());
        d.setFrecuencia(t.getFrecuencia());
        d.setDuracion(t.getDuracion());
        d.setIndicaciones(t.getIndicaciones());
        d.setIdHistorial(t.getHistorial().getIdHistorial());
        d.setIdMedicamento(t.getMedicamento().getIdMedicamento());
        d.setNombreMedicamento(t.getMedicamento().getNombre());
        return d;
    }
}
