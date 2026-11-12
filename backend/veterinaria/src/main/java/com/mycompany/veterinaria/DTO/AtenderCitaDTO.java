package com.mycompany.veterinaria.DTO;

/** Datos opcionales al atender una cita. Si viene 'diagnostico' se crea la historia clínica automáticamente. */
public class AtenderCitaDTO {

    private String diagnostico;
    private String observaciones;
    private Double peso;
    private Double temperatura;

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }
    public Double getTemperatura() { return temperatura; }
    public void setTemperatura(Double temperatura) { this.temperatura = temperatura; }
}
