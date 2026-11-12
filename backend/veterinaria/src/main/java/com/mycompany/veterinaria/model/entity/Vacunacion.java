
package com.mycompany.veterinaria.model.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vacunacion")
public class Vacunacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVacunacion;

    @ManyToOne
    @JoinColumn(name = "id_mascota", nullable = false)
    private Mascota mascota;

    @ManyToOne
    @JoinColumn(name = "id_vacuna", nullable = false)
    private Vacuna vacuna;

    @Column(nullable = false)
    private LocalDate fechaAplicacion;
    private LocalDate fechaProxima;
    @Column(length = 255)
    private String observaciones;

    // getters y setters

    public Long getIdVacunacion() {
        return idVacunacion;
    }

    public void setIdVacunacion(Long idVacunacion) {
        this.idVacunacion = idVacunacion;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    public Vacuna getVacuna() {
        return vacuna;
    }

    public void setVacuna(Vacuna vacuna) {
        this.vacuna = vacuna;
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
    
}