package com.mycompany.veterinaria.service;

import com.mycompany.veterinaria.DTO.MedicamentoDTO;
import com.mycompany.veterinaria.exception.NoEncontradoException;
import com.mycompany.veterinaria.exception.ReglaNegocioException;
import com.mycompany.veterinaria.model.entity.Medicamento;
import com.mycompany.veterinaria.util.Tx;
import com.mycompany.veterinaria.util.Validador;
import jakarta.persistence.EntityManager;
import java.util.List;

/** Catálogo de medicamentos. El nombre no se puede repetir. Eliminar falla con 409 si ya se usó en algún registro. */
public class MedicamentoService {

    private final AuditoriaService auditoria = new AuditoriaService();

    /** Lista el catálogo; con 'q' filtra por nombre. */
    public List<MedicamentoDTO> listar(String q) {
        String texto = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";
        return Tx.leer(em -> em.createQuery(
                "SELECT x FROM Medicamento x WHERE (:q IS NULL OR LOWER(x.nombre) LIKE :q) ORDER BY x.nombre", Medicamento.class)
                .setParameter("q", texto).getResultList().stream().map(this::aDTO).toList());
    }

    public MedicamentoDTO obtener(Long id) {
        return Tx.leer(em -> aDTO(existente(em, id)));
    }

    public MedicamentoDTO crear(MedicamentoDTO d, Long idActor) {
        MedicamentoDTO creado = Tx.ejecutar(em -> {
            Medicamento x = new Medicamento();
            copiar(em, d, x, null);
            em.persist(x);
            return aDTO(x);
        });
        auditoria.registrar(idActor, "CREAR", "Medicamento", creado.getIdMedicamento(), "Creación de medicamento: " + creado.getNombre());
        return creado;
    }

    public MedicamentoDTO actualizar(MedicamentoDTO d, Long idActor) {
        MedicamentoDTO actualizado = Tx.ejecutar(em -> {
            Medicamento x = existente(em, d.getIdMedicamento());
            copiar(em, d, x, x.getIdMedicamento());
            return aDTO(x);
        });
        auditoria.registrar(idActor, "ACTUALIZAR", "Medicamento", actualizado.getIdMedicamento(), "Actualización de medicamento: " + actualizado.getNombre());
        return actualizado;
    }

    public void eliminar(Long id, Long idActor) {
        String nombre = Tx.ejecutar(em -> {
            Medicamento x = existente(em, id);
            em.remove(x);
            return x.getNombre();
        });
        auditoria.registrar(idActor, "ELIMINAR", "Medicamento", id, "Eliminación de medicamento: " + nombre);
    }

    private Medicamento existente(EntityManager em, Long id) {
        Medicamento x = id == null ? null : em.find(Medicamento.class, id);
        if (x == null) {
            throw new NoEncontradoException("Medicamento no encontrado");
        }
        return x;
    }

    private void copiar(EntityManager em, MedicamentoDTO d, Medicamento x, Long excluirId) {
        String nombre = Validador.requerido(d.getNombre(), "nombre", 100);
        Long repetido = em.createQuery(
                "SELECT COUNT(y) FROM Medicamento y WHERE LOWER(y.nombre) = :n AND (:ex IS NULL OR y.idMedicamento <> :ex)", Long.class)
                .setParameter("n", nombre.toLowerCase()).setParameter("ex", excluirId)
                .setFlushMode(jakarta.persistence.FlushModeType.COMMIT).getSingleResult();
        if (repetido > 0) {
            throw new ReglaNegocioException("Ya existe un medicamento llamado '" + nombre + "'");
        }
        x.setNombre(nombre);
        x.setTipo(Validador.opcional(d.getTipo(), "tipo", 50));
        x.setPresentacion(Validador.opcional(d.getPresentacion(), "presentacion", 100));
        x.setDescripcion(Validador.opcional(d.getDescripcion(), "descripcion", 255));
    }

    private MedicamentoDTO aDTO(Medicamento x) {
        MedicamentoDTO d = new MedicamentoDTO();
        d.setIdMedicamento(x.getIdMedicamento());
        d.setNombre(x.getNombre());
        d.setTipo(x.getTipo());
        d.setPresentacion(x.getPresentacion());
        d.setDescripcion(x.getDescripcion());
        return d;
    }
}
