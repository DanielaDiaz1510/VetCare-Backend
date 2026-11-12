/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.veterinaria.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "vacuna", uniqueConstraints = @UniqueConstraint(name = "uk_vacuna_nombre", columnNames = "nombre"))
public class Vacuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVacuna;

    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(length = 255)
    private String descripcion;
    @Column(length = 50)
    private String especieAplicable;

    @JsonIgnore
    @OneToMany(mappedBy = "vacuna")
    private List<Vacunacion> vacunaciones;

    // getters y setters

    public Long getIdVacuna() {
        return idVacuna;
    }

    public void setIdVacuna(Long idVacuna) {
        this.idVacuna = idVacuna;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEspecieAplicable() {
        return especieAplicable;
    }

    public void setEspecieAplicable(String especieAplicable) {
        this.especieAplicable = especieAplicable;
    }

    public List<Vacunacion> getVacunaciones() {
        return vacunaciones;
    }

    public void setVacunaciones(List<Vacunacion> vacunaciones) {
        this.vacunaciones = vacunaciones;
    }
    
}
