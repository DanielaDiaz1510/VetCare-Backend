/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.veterinaria.DTO;

public class MedicamentoDTO {

    private Long idMedicamento;
    private String nombre;
    private String tipo;
    private String presentacion;
    private String descripcion;

    public MedicamentoDTO() {
    }

    public MedicamentoDTO(Long idMedicamento, String nombre, String tipo, String presentacion, String descripcion) {
        this.idMedicamento = idMedicamento;
        this.nombre = nombre;
        this.tipo = tipo;
        this.presentacion = presentacion;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters

    public Long getIdMedicamento() {
        return idMedicamento;
    }

    public void setIdMedicamento(Long idMedicamento) {
        this.idMedicamento = idMedicamento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
    