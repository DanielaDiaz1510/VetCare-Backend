package com.mycompany.veterinaria.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mycompany.veterinaria.DTO.HistorialClinicoDTO;
import com.mycompany.veterinaria.dao.VeterinarioDAO;
import com.mycompany.veterinaria.exception.AccesoDenegadoException;
import com.mycompany.veterinaria.exception.NoEncontradoException;
import com.mycompany.veterinaria.exception.ReglaNegocioException;
import com.mycompany.veterinaria.exception.ValidacionException;
import com.mycompany.veterinaria.model.entity.HistorialClinico;
import com.mycompany.veterinaria.model.entity.Mascota;
import com.mycompany.veterinaria.model.entity.Veterinario;
import com.mycompany.veterinaria.security.Permisos;
import com.mycompany.veterinaria.security.UsuarioAutenticado;
import com.mycompany.veterinaria.util.Json;
import com.mycompany.veterinaria.util.Tx;
import com.mycompany.veterinaria.util.Validador;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Historia clínica de cada mascota: cada atención es un registro con diagnóstico y signos vitales.
 * Regla: un VETERINARIO solo puede modificar las atenciones que él registró; el ADMIN puede todas.
 */
public class HistorialClinicoService {

    private final AuditoriaService auditoria = new AuditoriaService();

    /** Lista filtrando opcionalmente por mascota y/o veterinario, de la más reciente a la más antigua. */
    public List<HistorialClinicoDTO> listar(Long idMascota, Long idVeterinario) {
        return Tx.leer(em -> em.createQuery(
                "SELECT h FROM HistorialClinico h WHERE (:m IS NULL OR h.mascota.idMascota = :m)"
                + " AND (:v IS NULL OR h.veterinario.idVeterinario = :v) ORDER BY h.fechaAtencion DESC",
                HistorialClinico.class)
                .setParameter("m", idMascota).setParameter("v", idVeterinario)
                .getResultList().stream().map(HistorialClinicoService::aDTO).toList());
    }

    /** Detalle (RF-10): la atención y sus tratamientos. */
    public Map<String, Object> detalle(Long id) {
        return Tx.leer(em -> {
            HistorialClinico h = existente(em, id);
            Map<String, Object> m = new LinkedHashMap<>(Json.MAPPER.convertValue(aDTO(h), new TypeReference<Map<String, Object>>() { }));
            m.put("tratamientos", em.createQuery(
                    "SELECT t FROM Tratamiento t WHERE t.historial.idHistorial = :id ORDER BY t.idTratamiento",
                    com.mycompany.veterinaria.model.entity.Tratamiento.class)
                    .setParameter("id", id).getResultList().stream().map(TratamientoService::aDTO).toList());
            return m;
        });
    }

    public HistorialClinicoDTO crear(HistorialClinicoDTO d, UsuarioAutenticado actor) {
        HistorialClinicoDTO creado = Tx.ejecutar(em -> {
            HistorialClinico h = new HistorialClinico();
            h.setMascota(mascotaActiva(em, d.getIdMascota()));
            h.setVeterinario(veterinarioResponsable(em, d.getIdVeterinario(), actor));
            copiar(d, h);
            em.persist(h);
            return aDTO(h);
        });
        auditoria.registrar(actor.getId(), "CREAR", "HistorialClinico", creado.getIdHistorial(),
                "Atención clínica de " + creado.getNombreMascota());
        return creado;
    }

    public HistorialClinicoDTO actualizar(HistorialClinicoDTO d, UsuarioAutenticado actor) {
        HistorialClinicoDTO actualizado = Tx.ejecutar(em -> {
            HistorialClinico h = existente(em, d.getIdHistorial());
            comprobarPropiedad(em, h, actor);
            copiar(d, h);                                    // la mascota y el veterinario no cambian
            return aDTO(h);
        });
        auditoria.registrar(actor.getId(), "ACTUALIZAR", "HistorialClinico", actualizado.getIdHistorial(),
                "Actualización de atención de " + actualizado.getNombreMascota());
        return actualizado;
    }

    /** Solo ADMIN (lo exige Permisos). Los tratamientos se borran con la atención; las citas enlazadas se desvinculan. */
    public void eliminar(Long id, Long idActor) {
        Tx.ejecutar(em -> {
            HistorialClinico h = existente(em, id);
            em.createQuery("UPDATE Cita c SET c.historial = NULL WHERE c.historial.idHistorial = :id")
                    .setParameter("id", id).executeUpdate();
            em.remove(h);
            return null;
        });
        auditoria.registrar(idActor, "ELIMINAR", "HistorialClinico", id, "Eliminación de atención clínica");
    }

    // ------------------------------------------------------------------ internos (reutilizados por TratamientoService)

    static HistorialClinico existente(EntityManager em, Long id) {
        HistorialClinico h = id == null ? null : em.find(HistorialClinico.class, id);
        if (h == null) {
            throw new NoEncontradoException("Historia clínica no encontrada");
        }
        return h;
    }

    static void comprobarPropiedad(EntityManager em, HistorialClinico h, UsuarioAutenticado actor) {
        if (actor.tieneRol(Permisos.ADMIN)) {
            return;
        }
        Veterinario propio = new VeterinarioDAO(em).buscarPorUsuario(actor.getId());
        if (propio == null || !propio.getIdVeterinario().equals(h.getVeterinario().getIdVeterinario())) {
            throw new AccesoDenegadoException("Solo puede modificar las atenciones que usted registró");
        }
    }

    private Mascota mascotaActiva(EntityManager em, Long id) {
        if (id == null) {
            throw new ValidacionException("Falta el campo 'idMascota'");
        }
        Mascota m = em.find(Mascota.class, id);
        if (m == null) {
            throw new NoEncontradoException("Mascota no encontrada");
        }
        if (!"ACTIVO".equals(m.getEstado())) {
            throw new ReglaNegocioException("La mascota está inactiva: no admite nuevas atenciones");
        }
        return m;
    }

    /** ADMIN: indica el veterinario. VETERINARIO: queda registrado él mismo (no puede firmar por otro). */
    private Veterinario veterinarioResponsable(EntityManager em, Long idSolicitado, UsuarioAutenticado actor) {
        Veterinario v;
        if (actor.tieneRol(Permisos.ADMIN)) {
            if (idSolicitado == null) {
                throw new ValidacionException("Falta el campo 'idVeterinario'");
            }
            v = em.find(Veterinario.class, idSolicitado);
        } else {
            v = new VeterinarioDAO(em).buscarPorUsuario(actor.getId());
            if (v == null) {
                throw new AccesoDenegadoException("Su usuario no está asociado a un perfil de veterinario");
            }
            if (idSolicitado != null && !idSolicitado.equals(v.getIdVeterinario())) {
                throw new AccesoDenegadoException("Solo puede registrar atenciones a su nombre");
            }
        }
        if (v == null) {
            throw new NoEncontradoException("Veterinario no encontrado");
        }
        return v;
    }

    private void copiar(HistorialClinicoDTO d, HistorialClinico h) {
        LocalDateTime fecha = d.getFechaAtencion() == null ? LocalDateTime.now() : d.getFechaAtencion();
        if (fecha.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new ValidacionException("La fecha de atención no puede estar en el futuro");
        }
        h.setFechaAtencion(fecha.withNano(0));
        h.setMotivoConsulta(Validador.requerido(d.getMotivoConsulta(), "motivoConsulta", 255));
        h.setDiagnostico(Validador.opcional(d.getDiagnostico(), "diagnostico", 2000));
        h.setObservaciones(Validador.opcional(d.getObservaciones(), "observaciones", 2000));
        validarSignos(d.getPeso(), d.getTemperatura());
        h.setPeso(d.getPeso());
        h.setTemperatura(d.getTemperatura());
    }

    /** Rangos razonables: peso 0-500 kg y temperatura 30-45 °C. */
    public static void validarSignos(Double peso, Double temperatura) {
        if (peso != null && (peso <= 0 || peso > 500)) {
            throw new ValidacionException("El peso debe estar entre 0 y 500 kg");
        }
        if (temperatura != null && (temperatura < 30 || temperatura > 45)) {
            throw new ValidacionException("La temperatura debe estar entre 30 y 45 °C");
        }
    }

    static HistorialClinicoDTO aDTO(HistorialClinico h) {
        HistorialClinicoDTO d = new HistorialClinicoDTO();
        d.setIdHistorial(h.getIdHistorial());
        d.setFechaAtencion(h.getFechaAtencion());
        d.setMotivoConsulta(h.getMotivoConsulta());
        d.setDiagnostico(h.getDiagnostico());
        d.setObservaciones(h.getObservaciones());
        d.setPeso(h.getPeso());
        d.setTemperatura(h.getTemperatura());
        d.setIdMascota(h.getMascota().getIdMascota());
        d.setNombreMascota(h.getMascota().getNombre());
        d.setIdVeterinario(h.getVeterinario().getIdVeterinario());
        d.setNombreCompletoVeterinario(h.getVeterinario().getNombres() + " " + h.getVeterinario().getApellidos());
        return d;
    }
}
