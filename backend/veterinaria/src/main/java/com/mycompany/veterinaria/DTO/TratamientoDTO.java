
package com.mycompany.veterinaria.DTO;

public class TratamientoDTO {
    private Long idTratamiento;
    private String dosis;
    private String frecuencia;
    private Integer duracion;
    private String indicaciones;
    
    // Relaciones
    private Long idHistorial;
    private Long idMedicamento;
    private String nombreMedicamento;

    public TratamientoDTO() {
    }

    public TratamientoDTO(Long idTratamiento, String dosis, String frecuencia, Integer duracion, 
                          String indicaciones, Long idHistorial, Long idMedicamento, String nombreMedicamento) {
        this.idTratamiento = idTratamiento;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
        this.duracion = duracion;
        this.indicaciones = indicaciones;
        this.idHistorial = idHistorial;
        this.idMedicamento = idMedicamento;
        this.nombreMedicamento = nombreMedicamento;
    }
     // getters y setters

    public Long getIdTratamiento() {
        return idTratamiento;
    }

    public void setIdTratamiento(Long idTratamiento) {
        this.idTratamiento = idTratamiento;
    }

    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    public Long getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(Long idHistorial) {
        this.idHistorial = idHistorial;
    }

    public Long getIdMedicamento() {
        return idMedicamento;
    }

    public void setIdMedicamento(Long idMedicamento) {
        this.idMedicamento = idMedicamento;
    }

    public String getNombreMedicamento() {
        return nombreMedicamento;
    }

    public void setNombreMedicamento(String nombreMedicamento) {
        this.nombreMedicamento = nombreMedicamento;
    }
    
    
}
