package com.mycompany.veterinaria.DTO;

import java.time.LocalDateTime;

/**
 * Cita tal como viaja por la API (entrada y salida).
 * Al crear solo se envían: idMascota, idVeterinario, fechaHora, motivo (+ duracionMinutos y observaciones opcionales).
 * El estado lo controla el servidor: nace PROGRAMADA y solo cambia con /cancelar o /atender.
 */
public class CitaDTO {

    private Long idCita;
    private Long idMascota;
    private String nombreMascota;
    private Long idVeterinario;
    private String nombreVeterinario;
    private LocalDateTime fechaHora;
    private LocalDateTime fechaFin;
    private Integer duracionMinutos;
    private String motivo;
    private String estado;
    private String observaciones;
    private Long idHistorial;

    public Long getIdCita() { return idCita; }
    public void setIdCita(Long idCita) { this.idCita = idCita; }
    public Long getIdMascota() { return idMascota; }
    public void setIdMascota(Long idMascota) { this.idMascota = idMascota; }
    public String getNombreMascota() { return nombreMascota; }
    public void setNombreMascota(String nombreMascota) { this.nombreMascota = nombreMascota; }
    public Long getIdVeterinario() { return idVeterinario; }
    public void setIdVeterinario(Long idVeterinario) { this.idVeterinario = idVeterinario; }
    public String getNombreVeterinario() { return nombreVeterinario; }
    public void setNombreVeterinario(String nombreVeterinario) { this.nombreVeterinario = nombreVeterinario; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Long getIdHistorial() { return idHistorial; }
    public void setIdHistorial(Long idHistorial) { this.idHistorial = idHistorial; }
}
