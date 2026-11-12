
package com.mycompany.veterinaria.DTO;

public class MascotaAlergiaDTO {

    private Long id;
    private String observaciones;
    
    // Relaciones
    private Long idMascota;
    private String nombreMascota;
    private Long idAlergia;
    private String nombreAlergia;

    public MascotaAlergiaDTO() {
    }

    public MascotaAlergiaDTO(Long id, String observaciones, Long idMascota, String nombreMascota, Long idAlergia, String nombreAlergia) {
        this.id = id;
        this.observaciones = observaciones;
        this.idMascota = idMascota;
        this.nombreMascota = nombreMascota;
        this.idAlergia = idAlergia;
        this.nombreAlergia = nombreAlergia;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Long getIdMascota() {
        return idMascota;
    }

    public void setIdMascota(Long idMascota) {
        this.idMascota = idMascota;
    }

    public String getNombreMascota() {
        return nombreMascota;
    }

    public void setNombreMascota(String nombreMascota) {
        this.nombreMascota = nombreMascota;
    }

    public Long getIdAlergia() {
        return idAlergia;
    }

    public void setIdAlergia(Long idAlergia) {
        this.idAlergia = idAlergia;
    }

    public String getNombreAlergia() {
        return nombreAlergia;
    }

    public void setNombreAlergia(String nombreAlergia) {
        this.nombreAlergia = nombreAlergia;
    }
    
    
}