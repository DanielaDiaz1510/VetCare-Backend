package com.mycompany.veterinaria.service;

import com.mycompany.veterinaria.DTO.VeterinarioDTO;
import com.mycompany.veterinaria.dao.VeterinarioDAO;
import com.mycompany.veterinaria.exception.NoEncontradoException;
import com.mycompany.veterinaria.exception.ReglaNegocioException;
import com.mycompany.veterinaria.model.entity.Cita;
import com.mycompany.veterinaria.model.entity.Usuario;
import com.mycompany.veterinaria.model.entity.Veterinario;
import com.mycompany.veterinaria.util.Tx;
import com.mycompany.veterinaria.util.Validador;
import jakarta.persistence.EntityManager;
import java.util.List;

/** Reglas de negocio de los veterinarios. Se desactivan (no se borran) para conservar el historial que firmaron. */
public class VeterinarioService {

    private final AuditoriaService auditoria = new AuditoriaService();

    public List<VeterinarioDTO> listarTodosDTO() {
        return buscarDTO(null, null);
    }

    /** RF-09: por texto (nombre/documento) y estado. Sin estado se listan todos. */
    public List<VeterinarioDTO> buscarDTO(String q, String estado) {
        String texto = (q == null || q.isBlank()) ? null : q.trim();
        String est = (estado == null || estado.isBlank() || "TODOS".equalsIgnoreCase(estado))
                ? null : Validador.uno(estado, "estado", "ACTIVO", "INACTIVO");
        return Tx.leer(em -> new VeterinarioDAO(em).buscar(texto, est).stream().map(this::aDTO).toList());
    }

    public VeterinarioDTO obtenerDTO(Long id) {
        return Tx.leer(em -> aDTO(existente(em, id)));
    }

    public VeterinarioDTO crear(VeterinarioDTO d, Long idActor) {
        VeterinarioDTO creado = Tx.ejecutar(em -> {
            Veterinario v = new Veterinario();
            copiar(em, d, v);
            em.persist(v);
            return aDTO(v);
        });
        auditoria.registrar(idActor, "CREAR", "Veterinario", creado.getId(),
                "Creación de veterinario: " + creado.getNombres() + " " + creado.getApellidos());
        return creado;
    }

    public VeterinarioDTO actualizar(VeterinarioDTO d, Long idActor) {
        VeterinarioDTO actualizado = Tx.ejecutar(em -> {
            Veterinario v = existente(em, d.getId());
            copiar(em, d, v);
            return aDTO(v);
        });
        auditoria.registrar(idActor, "ACTUALIZAR", "Veterinario", actualizado.getId(),
                "Actualización de veterinario: " + actualizado.getNombres() + " " + actualizado.getApellidos());
        return actualizado;
    }

    public void desactivar(Long id, Long idActor) {
        String nombre = Tx.ejecutar(em -> {
            Veterinario v = existente(em, id);
            long pendientes = em.createQuery(
                    "SELECT COUNT(c) FROM Cita c WHERE c.veterinario.idVeterinario = :id AND c.estado = :e", Long.class)
                    .setParameter("id", id).setParameter("e", Cita.PROGRAMADA).getSingleResult();
            if (pendientes > 0) {
                throw new ReglaNegocioException("No se puede desactivar: el veterinario tiene " + pendientes
                        + " cita(s) programada(s). Cancélelas o reasígnelas primero");
            }
            v.setEstado("INACTIVO");
            return v.getNombres() + " " + v.getApellidos();
        });
        auditoria.registrar(idActor, "DESACTIVAR", "Veterinario", id, "Desactivación de veterinario: " + nombre);
    }

    // ------------------------------------------------------------------ internos

    private Veterinario existente(EntityManager em, Long id) {
        Veterinario v = id == null ? null : em.find(Veterinario.class, id);
        if (v == null) {
            throw new NoEncontradoException("Veterinario no encontrado");
        }
        return v;
    }

    private void copiar(EntityManager em, VeterinarioDTO d, Veterinario v) {
        v.setTipoDocumento(Validador.uno(d.getTipoDocumento(), "tipoDocumento", "CC", "CE", "PAS", "TI"));
        v.setNumeroDocumento(Validador.documento(d.getNumeroDocumento()));
        v.setNombres(Validador.requerido(d.getNombres(), "nombres", 100));
        v.setApellidos(Validador.requerido(d.getApellidos(), "apellidos", 100));
        v.setEspecialidad(Validador.opcional(d.getEspecialidad(), "especialidad", 100));
        v.setTelefono(Validador.telefonoOpcional(d.getTelefono()));
        v.setEmail(Validador.emailOpcional(d.getEmail()));
        v.setEstado(d.getEstado() == null || d.getEstado().isBlank()
                ? "ACTIVO" : Validador.uno(d.getEstado(), "estado", "ACTIVO", "INACTIVO"));

        if (new VeterinarioDAO(em).existeDocumento(v.getTipoDocumento(), v.getNumeroDocumento(), v.getIdVeterinario())) {
            throw new ReglaNegocioException("Ya existe un veterinario con el documento "
                    + v.getTipoDocumento() + " " + v.getNumeroDocumento());
        }

        // Cuenta de login opcional: debe existir y no estar ligada a otro veterinario
        if (d.getIdUsuario() == null) {
            v.setUsuario(null);
        } else {
            Usuario u = em.find(Usuario.class, d.getIdUsuario());
            if (u == null) {
                throw new NoEncontradoException("El usuario indicado no existe");
            }
            Veterinario otro = new VeterinarioDAO(em).buscarPorUsuario(u.getIdUsuario());
            if (otro != null && !otro.getIdVeterinario().equals(v.getIdVeterinario())) {
                throw new ReglaNegocioException("Ese usuario ya está asociado a otro veterinario");
            }
            v.setUsuario(u);
        }
    }

    private VeterinarioDTO aDTO(Veterinario v) {
        VeterinarioDTO d = new VeterinarioDTO(v.getIdVeterinario(), v.getTipoDocumento(), v.getNumeroDocumento(),
                v.getNombres(), v.getApellidos(), v.getEspecialidad(), v.getTelefono(), v.getEmail(), v.getEstado());
        d.setIdUsuario(v.getUsuario() == null ? null : v.getUsuario().getIdUsuario());
        return d;
    }
}
