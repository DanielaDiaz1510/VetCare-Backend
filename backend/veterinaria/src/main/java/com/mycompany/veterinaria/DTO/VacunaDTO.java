/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.veterinaria.DTO;

public class VacunaDTO {

    private Long idVacuna;
    private String nombre;
    private String descripcion;
    private String especieAplicable;

    // Constructor vacío obligatorio
    public VacunaDTO() {
    }

    // Constructor completo
    public VacunaDTO(Long idVacuna, String nombre, String descripcion, String especieAplicable) {
        this.idVacuna = idVacuna;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.especieAplicable = especieAplicable;
    }

    // Getters y Setters

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
    
}