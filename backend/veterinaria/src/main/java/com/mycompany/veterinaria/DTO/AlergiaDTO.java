/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.veterinaria.DTO;

public class AlergiaDTO {

    private Long idAlergia;
    private String nombre;
    private String descripcion;

    public AlergiaDTO() {
    }

    public AlergiaDTO(Long idAlergia, String nombre, String descripcion) {
        this.idAlergia = idAlergia;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
    
     // Getters y Setters

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

}
    