package cl.duoc.monitoriza.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class Informe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate fecha;
    @Lob
    private String contenidoJson;
    @Lob
    private String analisisJson;
    @Lob
    private byte[] pdfGenerado;
    private LocalDateTime fechaGeneracion;

    public Informe() {
         }

    public Informe(LocalDate fecha, String contenidoJson, String analisisJson, byte[] pdfGenerado, LocalDateTime fechaGeneracion) {
    this.fecha = fecha;
    this.contenidoJson = contenidoJson;
    this.analisisJson = analisisJson;
    this.pdfGenerado = pdfGenerado;
    this.fechaGeneracion = fechaGeneracion;
}


    public Informe(Long id, LocalDate fecha, String contenidoJson, String analisisJson, byte[] pdfGenerado, LocalDateTime fechaGeneracion) {
        this.id = id;
        this.fecha = fecha;
        this.contenidoJson = contenidoJson;
        this.analisisJson = analisisJson;
        this.pdfGenerado = pdfGenerado;
        this.fechaGeneracion = fechaGeneracion;
    }
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
    public byte[] getPdfGenerado() {
        return pdfGenerado;
    }
    public void setPdfGenerado(byte[] pdfGenerado) {
        this.pdfGenerado = pdfGenerado;
    }
    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }  
}
