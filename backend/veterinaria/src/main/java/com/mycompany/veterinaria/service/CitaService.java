package com.mycompany.veterinaria.service;

import com.mycompany.veterinaria.DTO.AtenderCitaDTO;
import com.mycompany.veterinaria.DTO.CitaDTO;
import com.mycompany.veterinaria.dao.CitaDAO;
import com.mycompany.veterinaria.dao.VeterinarioDAO;
import com.mycompany.veterinaria.exception.AccesoDenegadoException;
import com.mycompany.veterinaria.exception.NoEncontradoException;
import com.mycompany.veterinaria.exception.ReglaNegocioException;
import com.mycompany.veterinaria.exception.ValidacionException;
import com.mycompany.veterinaria.model.entity.Cita;
import com.mycompany.veterinaria.model.entity.HistorialClinico;
import com.mycompany.veterinaria.model.entity.Mascota;
import com.mycompany.veterinaria.model.entity.Veterinario;
import com.mycompany.veterinaria.security.Permisos;
import com.mycompany.veterinaria.security.UsuarioAutenticado;
import com.mycompany.veterinaria.util.Tx;
import com.mycompany.veterinaria.util.Validador;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Reglas de negocio de las citas (módulo propio del Grupo 7 en el PDF).
 *
 *  R1  No se agenda en el pasado.
 *  R2  La mascota y el veterinario deben estar ACTIVOS.
 *  R3  Un veterinario no puede tener dos citas que se crucen en el tiempo.
 *  R4  Una mascota no puede estar en dos citas que se crucen en el tiempo.
 *  R5  Solo se puede modificar, cancelar o atender una cita PROGRAMADA.
 *  R6  Una cita solo se atiende el día programado o después (no por adelantado).
 *  R7  Un VETERINARIO solo gestiona SUS citas; el ADMIN gestiona todas.
 *
 * Para que dos personas no reserven el mismo hueco a la vez, se bloquea la fila del veterinario
 * en la base de datos (bloqueo pesimista) mientras se comprueba el cruce y se guarda.
 */
public class CitaService {

    private static final int DURACION_DEFECTO = 30;
    private static final int DURACION_MIN = 10;
    private static final int DURACION_MAX = 240;

    private final AuditoriaService auditoria = new AuditoriaService();

    // ------------------------------------------------------------------ consultas

    /** RF-09: filtros combinables por estado, veterinario, mascota y fecha (un día). */
    public List<CitaDTO> buscarDTO(String estado, Long idVeterinario, Long idMascota, LocalDate fecha) {
        String est = estado == null ? null : Validador.uno(estado, "estado", Cita.PROGRAMADA, Cita.ATENDIDA, Cita.CANCELADA);
        LocalDateTime desde = fecha == null ? null : fecha.atStartOfDay();
        LocalDateTime hasta = fecha == null ? null : fecha.plusDays(1).atStartOfDay();
        return Tx.leer(em -> new CitaDAO(em).buscar(est, idVeterinario, idMascota, desde, hasta)
                .stream().map(this::aDTO).toList());
    }

    public CitaDTO obtenerDTO(Long id) {
        return Tx.leer(em -> aDTO(existente(em, id)));
    }

    // ------------------------------------------------------------------ crear

    public CitaDTO crear(CitaDTO d, UsuarioAutenticado actor) {
        CitaDTO creada = Tx.ejecutar(em -> {
            Long idVet = veterinarioDe(em, d.getIdVeterinario(), actor);
            Veterinario vet = bloquearVeterinario(em, idVet);
            Mascota mascota = mascotaActiva(em, d.getIdMascota());

            Cita c = new Cita();
            c.setMascota(mascota);
            c.setVeterinario(vet);
            aplicarDatos(c, d);
            validarCruces(em, c, null);
            em.persist(c);
            return aDTO(c);
        });
        auditoria.registrar(actor.getId(), "CREAR", "Cita", creada.getIdCita(),
                "Cita para " + creada.getNombreMascota() + " el " + creada.getFechaHora());
        return creada;
    }

    // ------------------------------------------------------------------ reprogramar / editar

    public CitaDTO actualizar(CitaDTO d, UsuarioAutenticado actor) {
        CitaDTO actualizada = Tx.ejecutar(em -> {
            Cita c = existente(em, d.getIdCita());
            comprobarPropiedad(em, c, actor);
            exigirProgramada(c, "modificar");

            // Reasignar a otro veterinario solo lo puede hacer un ADMIN
            Long idVetNuevo = d.getIdVeterinario() == null ? c.getVeterinario().getIdVeterinario() : d.getIdVeterinario();
            if (!idVetNuevo.equals(c.getVeterinario().getIdVeterinario())) {
                if (!actor.tieneRol(Permisos.ADMIN)) {
                    throw new AccesoDenegadoException("Solo un administrador puede reasignar la cita a otro veterinario");
                }
                c.setVeterinario(bloquearVeterinario(em, idVetNuevo));
            } else {
                bloquearVeterinario(em, idVetNuevo);
            }
            aplicarDatos(c, d);
            validarCruces(em, c, c.getIdCita());
            return aDTO(c);
        });
        auditoria.registrar(actor.getId(), "ACTUALIZAR", "Cita", actualizada.getIdCita(),
                "Reprogramación de cita para " + actualizada.getNombreMascota() + " al " + actualizada.getFechaHora());
        return actualizada;
    }

    // ------------------------------------------------------------------ cancelar

    public CitaDTO cancelar(Long id, String motivoCancelacion, UsuarioAutenticado actor) {
        CitaDTO cancelada = Tx.ejecutar(em -> {
            Cita c = existente(em, id);
            comprobarPropiedad(em, c, actor);
            exigirProgramada(c, "cancelar");
            c.setEstado(Cita.CANCELADA);
            String motivo = Validador.opcional(motivoCancelacion, "motivo", 200);
            if (motivo != null) {
                String previo = c.getObservaciones() == null ? "" : c.getObservaciones() + " | ";
                String nota = previo + "Cancelada: " + motivo;
                c.setObservaciones(nota.length() > 500 ? nota.substring(0, 500) : nota);
            }
            return aDTO(c);
        });
        auditoria.registrar(actor.getId(), "CANCELAR", "Cita", id, "Cancelación de cita de " + cancelada.getNombreMascota());
        return cancelada;
    }

    // ------------------------------------------------------------------ atender

    /**
     * Marca la cita como ATENDIDA. Si se envía diagnóstico, además crea la historia clínica con
     * el motivo de la cita y deja ambas enlazadas (trazabilidad).
     */
    public CitaDTO atender(Long id, AtenderCitaDTO datos, UsuarioAutenticado actor) {
        CitaDTO atendida = Tx.ejecutar(em -> {
            Cita c = existente(em, id);
            comprobarPropiedad(em, c, actor);
            exigirProgramada(c, "atender");
            if (c.getFechaHora().toLocalDate().isAfter(LocalDate.now())) {
                throw new ReglaNegocioException("No se puede atender por adelantado: la cita es el "
                        + c.getFechaHora().toLocalDate());
            }
            c.setEstado(Cita.ATENDIDA);

            if (datos != null && datos.getDiagnostico() != null && !datos.getDiagnostico().isBlank()) {
                HistorialClinico h = new HistorialClinico();
                h.setMascota(c.getMascota());
                h.setVeterinario(c.getVeterinario());
                h.setFechaAtencion(LocalDateTime.now());
                h.setMotivoConsulta(c.getMotivo());
                h.setDiagnostico(Validador.requerido(datos.getDiagnostico(), "diagnostico", 2000));
                h.setObservaciones(Validador.opcional(datos.getObservaciones(), "observaciones", 2000));
                HistorialClinicoService.validarSignos(datos.getPeso(), datos.getTemperatura());
                h.setPeso(datos.getPeso());
                h.setTemperatura(datos.getTemperatura());
                em.persist(h);
                c.setHistorial(h);
                if (datos.getPeso() != null) {
                    c.getMascota().setPesoActual(datos.getPeso());   // mantiene el peso actual de la mascota
                }
            }
            return aDTO(c);
        });
        auditoria.registrar(actor.getId(), "ATENDER", "Cita", id, "Cita atendida de " + atendida.getNombreMascota());
        return atendida;
    }

    // ------------------------------------------------------------------ internos

    private Cita existente(EntityManager em, Long id) {
        Cita c = id == null ? null : em.find(Cita.class, id);
        if (c == null) {
            throw new NoEncontradoException("Cita no encontrada");
        }
        return c;
    }

    private void exigirProgramada(Cita c, String accion) {
        if (!Cita.PROGRAMADA.equals(c.getEstado())) {
            throw new ReglaNegocioException("No se puede " + accion + " una cita en estado " + c.getEstado()
                    + ". Solo las citas PROGRAMADAS admiten cambios");
        }
    }

    /** R7: un VETERINARIO solo opera sobre sus propias citas. */
    private void comprobarPropiedad(EntityManager em, Cita c, UsuarioAutenticado actor) {
        if (actor.tieneRol(Permisos.ADMIN)) {
            return;
        }
        Veterinario propio = new VeterinarioDAO(em).buscarPorUsuario(actor.getId());
        if (propio == null || !propio.getIdVeterinario().equals(c.getVeterinario().getIdVeterinario())) {
            throw new AccesoDenegadoException("Solo puede gestionar las citas asignadas a usted");
        }
    }

    /** Determina el veterinario de la cita: el ADMIN elige; el VETERINARIO queda como responsable de sus propias citas. */
    private Long veterinarioDe(EntityManager em, Long idSolicitado, UsuarioAutenticado actor) {
        if (actor.tieneRol(Permisos.ADMIN)) {
            if (idSolicitado == null) {
                throw new ValidacionException("Falta el campo 'idVeterinario'");
            }
            return idSolicitado;
        }
        Veterinario propio = new VeterinarioDAO(em).buscarPorUsuario(actor.getId());
        if (propio == null) {
            throw new AccesoDenegadoException("Su usuario no está asociado a un perfil de veterinario");
        }
        if (idSolicitado != null && !idSolicitado.equals(propio.getIdVeterinario())) {
            throw new AccesoDenegadoException("Solo puede agendar citas a su nombre");
        }
        return propio.getIdVeterinario();
    }

    /** Bloquea la fila del veterinario hasta terminar la transacción y verifica que esté activo (R2). */
    private Veterinario bloquearVeterinario(EntityManager em, Long id) {
        Veterinario v = em.find(Veterinario.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (v == null) {
            throw new NoEncontradoException("Veterinario no encontrado");
        }
        if (!"ACTIVO".equals(v.getEstado())) {
            throw new ReglaNegocioException("El veterinario está inactivo y no puede recibir citas");
        }
        return v;
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
            throw new ReglaNegocioException("La mascota está inactiva y no puede recibir citas");
        }
        return m;
    }

    /** Valida y copia fecha, duración, motivo y observaciones (R1 y rangos). */
    private void aplicarDatos(Cita c, CitaDTO d) {
        if (d.getFechaHora() == null) {
            throw new ValidacionException("Falta el campo 'fechaHora' (formato AAAA-MM-DDThh:mm:ss)");
        }
        if (!d.getFechaHora().isAfter(LocalDateTime.now())) {
            throw new ValidacionException("La cita debe programarse en una fecha y hora futuras");
        }
        int duracion = d.getDuracionMinutos() == null ? DURACION_DEFECTO : d.getDuracionMinutos();
        if (duracion < DURACION_MIN || duracion > DURACION_MAX) {
            throw new ValidacionException("La duración debe estar entre " + DURACION_MIN + " y " + DURACION_MAX + " minutos");
        }
        c.setFechaHora(d.getFechaHora().withNano(0));
        c.setDuracionMinutos(duracion);
        c.setMotivo(Validador.requerido(d.getMotivo(), "motivo", 255));
        c.setObservaciones(Validador.opcional(d.getObservaciones(), "observaciones", 500));
        if (c.getEstado() == null) {
            c.setEstado(Cita.PROGRAMADA);
        }
    }

    /** R3 y R4: dos citas se cruzan si cada una empieza antes de que la otra termine. */
    private void validarCruces(EntityManager em, Cita nueva, Long excluirId) {
        LocalDateTime inicio = nueva.getFechaHora();
        LocalDateTime fin = nueva.getFechaFin();
        // Candidatas: las que empiezan desde 4 h antes (duración máxima) hasta que termina la nueva
        List<Cita> candidatas = new CitaDAO(em).programadasEnVentana(
                nueva.getVeterinario().getIdVeterinario(), nueva.getMascota().getIdMascota(),
                inicio.minusMinutes(DURACION_MAX), fin, excluirId);
        for (Cita otra : candidatas) {
            boolean seCruzan = inicio.isBefore(otra.getFechaFin()) && otra.getFechaHora().isBefore(fin);
            if (!seCruzan) {
                continue;
            }
            if (otra.getVeterinario().getIdVeterinario().equals(nueva.getVeterinario().getIdVeterinario())) {
                throw new ReglaNegocioException("El veterinario ya tiene una cita que se cruza con ese horario ("
                        + otra.getFechaHora() + " a " + otra.getFechaFin().toLocalTime() + ")");
            }
            throw new ReglaNegocioException("La mascota ya tiene una cita que se cruza con ese horario ("
                    + otra.getFechaHora() + ")");
        }
    }

    private CitaDTO aDTO(Cita c) {
        CitaDTO d = new CitaDTO();
        d.setIdCita(c.getIdCita());
        d.setIdMascota(c.getMascota().getIdMascota());
        d.setNombreMascota(c.getMascota().getNombre());
        d.setIdVeterinario(c.getVeterinario().getIdVeterinario());
        d.setNombreVeterinario(c.getVeterinario().getNombres() + " " + c.getVeterinario().getApellidos());
        d.setFechaHora(c.getFechaHora());
        d.setFechaFin(c.getFechaFin());
        d.setDuracionMinutos(c.getDuracionMinutos());
        d.setMotivo(c.getMotivo());
        d.setEstado(c.getEstado());
        d.setObservaciones(c.getObservaciones());
        d.setIdHistorial(c.getHistorial() == null ? null : c.getHistorial().getIdHistorial());
        return d;
    }
}
