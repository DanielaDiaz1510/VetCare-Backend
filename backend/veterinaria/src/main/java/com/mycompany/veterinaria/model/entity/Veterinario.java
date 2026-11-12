package com.mycompany.veterinaria.model.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "veterinario", uniqueConstraints = @UniqueConstraint(name = "uk_veterinario_documento", columnNames = {"tipoDocumento", "numeroDocumento"}))
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVeterinario;

    @Column(nullable = false, length = 5)
    private String tipoDocumento;
    @Column(nullable = false, length = 30)
    private String numeroDocumento;
    @Column(nullable = false, length = 100)
    private String nombres;
    @Column(nullable = false, length = 100)
    private String apellidos;
    @Column(length = 100)
    private String especialidad;
    @Column(length = 30)
    private String telefono;
    @Column(length = 120)
    private String email;
    @Column(nullable = false, length = 10)
    private String estado;

    // Cuenta de acceso (login) asociada a este profesional. Es opcional y única.
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "id_usuario", unique = true)
    private Usuario usuario;

    @JsonIgnore
    @OneToMany(mappedBy = "veterinario")
    private List<HistorialClinico> historiales;

    // getters y setters

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getIdVeterinario() {
        return idVeterinario;
    }

    public void setIdVeterinario(Long idVeterinario) {
        this.idVeterinario = idVeterinario;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
