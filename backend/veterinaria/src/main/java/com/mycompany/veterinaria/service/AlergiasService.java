package com.mycompany.veterinaria.service;

import com.mycompany.veterinaria.DTO.AlergiaDTO;
import com.mycompany.veterinaria.exception.NoEncontradoException;
import com.mycompany.veterinaria.exception.ReglaNegocioException;
import com.mycompany.veterinaria.model.entity.Alergia;
import com.mycompany.veterinaria.util.Tx;
import com.mycompany.veterinaria.util.Validador;
import jakarta.persistence.EntityManager;
import java.util.List;

/** Catálogo de alergias. El nombre no se puede repetir. Eliminar falla con 409 si ya se usó en algún registro. */
public class AlergiasService {

    private final AuditoriaService auditoria = new AuditoriaService();

    /** Lista el catálogo; con 'q' filtra por nombre. */
    public List<AlergiaDTO> listar(String q) {
        String texto = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";
        return Tx.leer(em -> em.createQuery(
                "SELECT x FROM Alergia x WHERE (:q IS NULL OR LOWER(x.nombre) LIKE :q) ORDER BY x.nombre", Alergia.class)
                .setParameter("q", texto).getResultList().stream().map(this::aDTO).toList());
    }

    public AlergiaDTO obtener(Long id) {
        return Tx.leer(em -> aDTO(existente(em, id)));
    }

    public AlergiaDTO crear(AlergiaDTO d, Long idActor) {
        AlergiaDTO creado = Tx.ejecutar(em -> {
            Alergia x = new Alergia();
            copiar(em, d, x, null);
            em.persist(x);
            return aDTO(x);
        });
        auditoria.registrar(idActor, "CREAR", "Alergia", creado.getIdAlergia(), "Creación de alergia: " + creado.getNombre());
        return creado;
    }

    public AlergiaDTO actualizar(AlergiaDTO d, Long idActor) {
        AlergiaDTO actualizado = Tx.ejecutar(em -> {
            Alergia x = existente(em, d.getIdAlergia());
            copiar(em, d, x, x.getIdAlergia());
            return aDTO(x);
        });
        auditoria.registrar(idActor, "ACTUALIZAR", "Alergia", actualizado.getIdAlergia(), "Actualización de alergia: " + actualizado.getNombre());
        return actualizado;
    }

    public void eliminar(Long id, Long idActor) {
        String nombre = Tx.ejecutar(em -> {
            Alergia x = existente(em, id);
            em.remove(x);
            return x.getNombre();
        });
        auditoria.registrar(idActor, "ELIMINAR", "Alergia", id, "Eliminación de alergia: " + nombre);
    }

    private Alergia existente(EntityManager em, Long id) {
        Alergia x = id == null ? null : em.find(Alergia.class, id);
        if (x == null) {
            throw new NoEncontradoException("Alergia no encontrada");
        }
        return x;
    }

    private void copiar(EntityManager em, AlergiaDTO d, Alergia x, Long excluirId) {
        String nombre = Validador.requerido(d.getNombre(), "nombre", 100);
        Long repetido = em.createQuery(
                "SELECT COUNT(y) FROM Alergia y WHERE LOWER(y.nombre) = :n AND (:ex IS NULL OR y.idAlergia <> :ex)", Long.class)
                .setParameter("n", nombre.toLowerCase()).setParameter("ex", excluirId)
                .setFlushMode(jakarta.persistence.FlushModeType.COMMIT).getSingleResult();
        if (repetido > 0) {
            throw new ReglaNegocioException("Ya existe una alergia llamada '" + nombre + "'");
        }
        x.setNombre(nombre);
        x.setDescripcion(Validador.opcional(d.getDescripcion(), "descripcion", 255));
    }

    private AlergiaDTO aDTO(Alergia x) {
        AlergiaDTO d = new AlergiaDTO();
        d.setIdAlergia(x.getIdAlergia());
        d.setNombre(x.getNombre());
        d.setDescripcion(x.getDescripcion());
        return d;
    }
}
