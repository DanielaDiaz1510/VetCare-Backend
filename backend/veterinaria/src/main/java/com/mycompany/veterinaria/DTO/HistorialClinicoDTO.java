/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.veterinaria.DTO;

import java.time.LocalDateTime;

public class HistorialClinicoDTO {

    private Long idHistorial;
    private LocalDateTime fechaAtencion;
    private String motivoConsulta;
    private String diagnostico;
    private String observaciones;
    private Double peso;
    private Double temperatura;
    
    // Relaciones (IDs y Nombres para la vista)
    private Long idMascota;
    private String nombreMascota;
    private Long idVeterinario;
    private String nombreCompletoVeterinario;

    public HistorialClinicoDTO() {
    }

    public HistorialClinicoDTO(Long idHistorial, LocalDateTime fechaAtencion, String motivoConsulta, 
                              String diagnostico, String observaciones, Double peso, Double temperatura, 
                              Long idMascota, String nombreMascota, Long idVeterinario, String nombreCompletoVeterinario) {
        this.idHistorial = idHistorial;
        this.fechaAtencion = fechaAtencion;
        this.motivoConsulta = motivoConsulta;
        this.diagnostico = diagnostico;
        this.observaciones = observaciones;
        this.peso = peso;
        this.temperatura = temperatura;
        this.idMascota = idMascota;
        this.nombreMascota = nombreMascota;
        this.idVeterinario = idVeterinario;
        this.nombreCompletoVeterinario = nombreCompletoVeterinario;
    }
     // getters y setters

    public Long getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(Long idHistorial) {
        this.idHistorial = idHistorial;
    }

    public LocalDateTime getFechaAtencion() {
        return fechaAtencion;
    }

    public void setFechaAtencion(LocalDateTime fechaAtencion) {
        this.fechaAtencion = fechaAtencion;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Long getIdMascota() {
        return idMascota;
    }

    public void setIdMascota(Long idMascota) {
        this.idMascota = idMascota;
    }

    public String getNombreMascota() {
        return nombreMascota;
    }

    public void setNombreMascota(String nombreMascota) {
        this.nombreMascota = nombreMascota;
    }

    public Long getIdVeterinario() {
        return idVeterinario;
    }

    public void setIdVeterinario(Long idVeterinario) {
        this.idVeterinario = idVeterinario;
    }

    public String getNombreCompletoVeterinario() {
        return nombreCompletoVeterinario;
    }

    public void setNombreCompletoVeterinario(String nombreCompletoVeterinario) {
        this.nombreCompletoVeterinario = nombreCompletoVeterinario;
    }
    
    
}
