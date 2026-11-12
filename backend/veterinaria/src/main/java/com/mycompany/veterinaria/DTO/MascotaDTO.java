package com.mycompany.veterinaria.DTO;

import java.time.LocalDate;

public class MascotaDTO {

    private Long id;
    private String nombre;
    private String especie;
    private String raza;
    private LocalDate fechaNacimiento;
    private String sexo;
    private String color;
    private Double pesoActual;
    private String estado;
    private Long idPropietario;          // ID del dueño
    private String nombrePropietario;    // Nombre completo del dueño (para mostrar en tablas)

    public MascotaDTO() {
    }

    public MascotaDTO(Long id, String nombre, String especie, String raza, LocalDate fechaNacimiento,
                      String sexo, String color, Double pesoActual, String estado,
                      Long idPropietario, String nombrePropietario) {
        this.id = id;
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.color = color;
        this.pesoActual = pesoActual;
        this.estado = estado;
        this.idPropietario = idPropietario;
        this.nombrePropietario = nombrePropietario;
    }

    // getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Double getPesoActual() { return pesoActual; }
    public void setPesoActual(Double pesoActual) { this.pesoActual = pesoActual; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Long getIdPropietario() { return idPropietario; }
    public void setIdPropietario(Long idPropietario) { this.idPropietario = idPropietario; }

    public String getNombrePropietario() { return nombrePropietario; }
    public void setNombrePropietario(String nombrePropietario) { this.nombrePropietario = nombrePropietario; }
}
