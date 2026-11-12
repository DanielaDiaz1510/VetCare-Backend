package com.mycompany.veterinaria.DTO;

import java.time.LocalDateTime;

public class AuditoriaDTO {

    private Long idAuditoria;
    private Long idUsuario;
    private String username;
    private String accion;
    private String entidad;
    private Long idRegistro;
    private LocalDateTime fechaHora;
    private String descripcion;

    public AuditoriaDTO() {
    }

    public AuditoriaDTO(
            Long idAuditoria,
            Long idUsuario,
            String username,
            String accion,
            String entidad,
            Long idRegistro,
            LocalDateTime fechaHora,
            String descripcion) {

        this.idAuditoria = idAuditoria;
        this.idUsuario = idUsuario;
        this.username = username;
        this.accion = accion;
        this.entidad = entidad;
        this.idRegistro = idRegistro;
        this.fechaHora = fechaHora;
        this.descripcion = descripcion;
    }

    // getters y setters

    public Long getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Long idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public Long getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Long idRegistro) {
        this.idRegistro = idRegistro;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
