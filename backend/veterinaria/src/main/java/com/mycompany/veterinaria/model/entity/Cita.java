package com.mycompany.veterinaria.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Cita veterinaria: une una mascota con un veterinario en una fecha y hora.
 *
 * Estados posibles: PROGRAMADA -> ATENDIDA  o  PROGRAMADA -> CANCELADA.
 * Una cita ATENDIDA o CANCELADA ya no se puede modificar (regla de negocio).
 */
@Entity
@Table(name = "cita", indexes = {
    @Index(name = "idx_cita_fecha", columnList = "fechaHora"),
    @Index(name = "idx_cita_estado", columnList = "estado")
})
public class Cita {

    public static final String PROGRAMADA = "PROGRAMADA";
    public static final String ATENDIDA = "ATENDIDA";
    public static final String CANCELADA = "CANCELADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCita;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_mascota", nullable = false)
    private Mascota mascota;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_veterinario", nullable = false)
    private Veterinario veterinario;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private Integer duracionMinutos = 30;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Column(nullable = false, length = 15)
    private String estado = PROGRAMADA;

    @Column(length = 500)
    private String observaciones;

    // Historia clínica generada al atender la cita (puede ser nula)
    @ManyToOne
    @JoinColumn(name = "id_historial")
    private HistorialClinico historial;

    public Long getIdCita() { return idCita; }
    public void setIdCita(Long idCita) { this.idCita = idCita; }

    public Mascota getMascota() { return mascota; }
    public void setMascota(Mascota mascota) { this.mascota = mascota; }

    public Veterinario getVeterinario() { return veterinario; }
    public void setVeterinario(Veterinario veterinario) { this.veterinario = veterinario; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public HistorialClinico getHistorial() { return historial; }
    public void setHistorial(HistorialClinico historial) { this.historial = historial; }

    /** Momento en que termina la cita (inicio + duración). */
    public LocalDateTime getFechaFin() {
        return fechaHora.plusMinutes(duracionMinutos == null ? 30 : duracionMinutos);
    }
}
