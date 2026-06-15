package cl.duoc.monitoriza.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InformeDetalleDto {

    private Long id;
    private LocalDate fecha;
    private String contenidoJson;
    private String analisisJson;
    private LocalDateTime fechaGeneracion;
    private boolean tienePdf;

    public InformeDetalleDto() {}

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

    public String getContenidoJson() {
        return contenidoJson;
    }

    public void setContenidoJson(String contenidoJson) {
        this.contenidoJson = contenidoJson;
    }

    public String getAnalisisJson() {
        return analisisJson;
    }

    public void setAnalisisJson(String analisisJson) {
        this.analisisJson = analisisJson;
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
}