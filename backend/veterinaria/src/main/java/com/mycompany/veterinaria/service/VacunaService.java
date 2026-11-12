package com.mycompany.veterinaria.service;

import com.mycompany.veterinaria.DTO.VacunaDTO;
import com.mycompany.veterinaria.exception.NoEncontradoException;
import com.mycompany.veterinaria.exception.ReglaNegocioException;
import com.mycompany.veterinaria.model.entity.Vacuna;
import com.mycompany.veterinaria.util.Tx;
import com.mycompany.veterinaria.util.Validador;
import jakarta.persistence.EntityManager;
import java.util.List;

/** Catálogo de vacunas. El nombre no se puede repetir. Eliminar falla con 409 si ya se usó en algún registro. */
public class VacunaService {

    private final AuditoriaService auditoria = new AuditoriaService();

    /** Lista el catálogo; con 'q' filtra por nombre. */
    public List<VacunaDTO> listar(String q) {
        String texto = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";
        return Tx.leer(em -> em.createQuery(
                "SELECT x FROM Vacuna x WHERE (:q IS NULL OR LOWER(x.nombre) LIKE :q) ORDER BY x.nombre", Vacuna.class)
                .setParameter("q", texto).getResultList().stream().map(this::aDTO).toList());
    }

    public VacunaDTO obtener(Long id) {
        return Tx.leer(em -> aDTO(existente(em, id)));
    }

    public VacunaDTO crear(VacunaDTO d, Long idActor) {
        VacunaDTO creado = Tx.ejecutar(em -> {
            Vacuna x = new Vacuna();
            copiar(em, d, x, null);
            em.persist(x);
            return aDTO(x);
        });
        auditoria.registrar(idActor, "CREAR", "Vacuna", creado.getIdVacuna(), "Creación de vacuna: " + creado.getNombre());
        return creado;
    }

    public VacunaDTO actualizar(VacunaDTO d, Long idActor) {
        VacunaDTO actualizado = Tx.ejecutar(em -> {
            Vacuna x = existente(em, d.getIdVacuna());
            copiar(em, d, x, x.getIdVacuna());
            return aDTO(x);
        });
        auditoria.registrar(idActor, "ACTUALIZAR", "Vacuna", actualizado.getIdVacuna(), "Actualización de vacuna: " + actualizado.getNombre());
        return actualizado;
    }

    public void eliminar(Long id, Long idActor) {
        String nombre = Tx.ejecutar(em -> {
            Vacuna x = existente(em, id);
            em.remove(x);
            return x.getNombre();
        });
        auditoria.registrar(idActor, "ELIMINAR", "Vacuna", id, "Eliminación de vacuna: " + nombre);
    }

    private Vacuna existente(EntityManager em, Long id) {
        Vacuna x = id == null ? null : em.find(Vacuna.class, id);
        if (x == null) {
            throw new NoEncontradoException("Vacuna no encontrada");
        }
        return x;
    }

    private void copiar(EntityManager em, VacunaDTO d, Vacuna x, Long excluirId) {
        String nombre = Validador.requerido(d.getNombre(), "nombre", 100);
        Long repetido = em.createQuery(
                "SELECT COUNT(y) FROM Vacuna y WHERE LOWER(y.nombre) = :n AND (:ex IS NULL OR y.idVacuna <> :ex)", Long.class)
                .setParameter("n", nombre.toLowerCase()).setParameter("ex", excluirId)
                .setFlushMode(jakarta.persistence.FlushModeType.COMMIT).getSingleResult();
        if (repetido > 0) {
            throw new ReglaNegocioException("Ya existe una vacuna llamada '" + nombre + "'");
        }
        x.setNombre(nombre);
        x.setDescripcion(Validador.opcional(d.getDescripcion(), "descripcion", 255));
        x.setEspecieAplicable(Validador.opcional(d.getEspecieAplicable(), "especieAplicable", 50));
    }

    private VacunaDTO aDTO(Vacuna x) {
        VacunaDTO d = new VacunaDTO();
        d.setIdVacuna(x.getIdVacuna());
        d.setNombre(x.getNombre());
        d.setDescripcion(x.getDescripcion());
        d.setEspecieAplicable(x.getEspecieAplicable());
        return d;
    }
}
