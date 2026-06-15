package cl.duoc.monitoriza.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InformeListadoDto {

    private Long id;
    private LocalDate fecha;
    private LocalDateTime fechaGeneracion;
    private boolean tienePdf;
    private boolean tieneAnalisis;

    public InformeListadoDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public boolean isTienePdf() {
        return tienePdf;
    }

    public void setTienePdf(boolean tienePdf) {
        this.tienePdf = tienePdf;
    }

    public boolean isTieneAnalisis() {
        return tieneAnalisis;
    }

    public void setTieneAnalisis(boolean tieneAnalisis) {
        this.tieneAnalisis = tieneAnalisis;
    }
}