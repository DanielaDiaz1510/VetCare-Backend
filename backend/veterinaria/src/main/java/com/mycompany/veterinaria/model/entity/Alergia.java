/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.veterinaria.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "alergia", uniqueConstraints = @UniqueConstraint(name = "uk_alergia_nombre", columnNames = "nombre"))
public class Alergia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAlergia;

    @Column(nullable = false, length = 100)
    private String nombre;
    private String descripcion;

    @JsonIgnore
    @OneToMany(mappedBy = "alergia")
    private List<MascotaAlergia> mascotas;

    // getters y setters

    public Long getIdAlergia() {
        return idAlergia;
    }

    public void setIdAlergia(Long idAlergia) {
        this.idAlergia = idAlergia;
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

    public List<MascotaAlergia> getMascotas() {
        return mascotas;
    }

    public void setMascotas(List<MascotaAlergia> mascotas) {
        this.mascotas = mascotas;
    }
    
}