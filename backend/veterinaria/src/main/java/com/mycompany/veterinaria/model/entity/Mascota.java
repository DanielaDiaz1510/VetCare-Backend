package com.mycompany.veterinaria.model.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "mascota")
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonAlias("id")   // la API devuelve "id"; así también se acepta "id" al editar
    private Long idMascota;

    @ManyToOne
    @JoinColumn(name = "id_propietario", nullable = false)
    private Propietario propietario;

    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(nullable = false, length = 50)
    private String especie;
    @Column(length = 80)
    private String raza;
    @Column(length = 1)
    private String sexo;
    private LocalDate fechaNacimiento;
    @Column(length = 50)
    private String color;
    private Double pesoActual;
    private Boolean esterilizado;
    @Column(nullable = false, length = 10)
    private String estado = "ACTIVO";

    @JsonIgnore
    @OneToMany(mappedBy = "mascota")
    private List<HistorialClinico> historiales;

    // getters y setters

    /**
     * Permite enviar el propietario de forma plana ({"idPropietario": 6}), que es como lo devuelve la API,
     * además de la forma anidada ({"propietario": {"idPropietario": 6}}).
     */
    @JsonSetter("idPropietario")
    public void setIdPropietarioPlano(Long idPropietario) {
        if (idPropietario != null) {
            Propietario p = new Propietario();
            p.setIdPropietario(idPropietario);
            this.propietario = p;
        }
    }

    public Long getIdMascota() {
        return idMascota;
    }

    public void setIdMascota(Long idMascota) {
        this.idMascota = idMascota;
    }

    public Propietario getPropietario() {
        return propietario;
    }

    public void setPropietario(Propietario propietario) {
        this.propietario = propietario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Double getPesoActual() {
        return pesoActual;
    }

    public void setPesoActual(Double pesoActual) {
        this.pesoActual = pesoActual;
    }

    public Boolean getEsterilizado() {
        return esterilizado;
    }

    public void setEsterilizado(Boolean esterilizado) {
        this.esterilizado = esterilizado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<HistorialClinico> getHistoriales() {
        return historiales;
    }

    public void setHistoriales(List<HistorialClinico> historiales) {
        this.historiales = historiales;
    }
    
    
}