
package com.mycompany.veterinaria.DTO;

import java.time.LocalDate;

public class VacunacionDTO {

    private Long idVacunacion;
    private LocalDate fechaAplicacion;
    private LocalDate fechaProxima;
    private String observaciones;
    
    // Relaciones
    private Long idMascota;
    private String nombreMascota;
    private Long idVacuna;
    private String nombreVacuna;

    public VacunacionDTO() {
    }

    public VacunacionDTO(Long idVacunacion, LocalDate fechaAplicacion, LocalDate fechaProxima, 
                         String observaciones, Long idMascota, String nombreMascota, Long idVacuna, String nombreVacuna) {
        this.idVacunacion = idVacunacion;
        this.fechaAplicacion = fechaAplicacion;
        this.fechaProxima = fechaProxima;
        this.observaciones = observaciones;
        this.idMascota = idMascota;
        this.nombreMascota = nombreMascota;
        this.idVacuna = idVacuna;
        this.nombreVacuna = nombreVacuna;
    }
    
    // Getters y Setters

    public Long getIdVacunacion() {
        return idVacunacion;
    }

    public void setIdVacunacion(Long idVacunacion) {
        this.idVacunacion = idVacunacion;
    }

    public LocalDate getFechaAplicacion() {
        return fechaAplicacion;
    }

    public void setFechaAplicacion(LocalDate fechaAplicacion) {
        this.fechaAplicacion = fechaAplicacion;
    }

    public LocalDate getFechaProxima() {
        return fechaProxima;
    }

    public void setFechaProxima(LocalDate fechaProxima) {
        this.fechaProxima = fechaProxima;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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

    public Long getIdVacuna() {
        return idVacuna;
    }

    public void setIdVacuna(Long idVacuna) {
        this.idVacuna = idVacuna;
    }

    public String getNombreVacuna() {
        return nombreVacuna;
    }

    public void setNombreVacuna(String nombreVacuna) {
        this.nombreVacuna = nombreVacuna;
    }

}
    
