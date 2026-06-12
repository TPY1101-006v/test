package cl.duoc.monitoriza.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;

@Entity
public class Medicion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMedicion;

    private Double temperatura;
    private Double humedad;
    private Double db;
    private Double lux;
    private Double eco2;
    private Double tvoc;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;
    
    @PrePersist
    void asignarFechaSiFalta() {
        if (this.fechaHora == null) {
            this.fechaHora = LocalDateTime.now();
        }
    }

    @ManyToOne
    @JoinColumn(name="nodo_id")
    @JsonBackReference
    private Nodo nodo;

    public Medicion() {
    }

    public Medicion(Long idMedicion, Double temperatura, Double humedad, Double db, Double lux, Double eco2,
            Double tvoc, LocalDateTime fechaHora, Nodo nodo) {
        this.idMedicion = idMedicion;
        this.temperatura = temperatura;
        this.humedad = humedad;
        this.db = db;
        this.lux = lux;
        this.eco2 = eco2;
        this.tvoc = tvoc;
        this.fechaHora = fechaHora;
        this.nodo = nodo;
    }

    // Constructor sin idMedicion (generado automatico), ni fechaHora para cuando el ESP8266 mande datos
    public Medicion(Double temperatura, Double humedad, Double db, Double lux, Double eco2,
            Double tvoc, Nodo nodo) {
        this.temperatura = temperatura;
        this.humedad = humedad;
        this.db = db;
        this.lux = lux;
        this.eco2 = eco2;
        this.tvoc = tvoc;
        this.nodo = nodo;
    }

    public Long getIdMedicion() {
        return idMedicion;
    }

    public void setIdMedicion(Long idMedicion) {
        this.idMedicion = idMedicion;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Double getHumedad() {
        return humedad;
    }

    public void setHumedad(Double humedad) {
        this.humedad = humedad;
    }

    public Double getDb() {
        return db;
    }

    public void setDb(Double db) {
        this.db = db;
    }

    public Double getLux() {
        return lux;
    }

    public void setLux(Double lux) {
        this.lux = lux;
    }

    public Double getEco2() {
        return eco2;
    }

    public void setEco2(Double eco2) {
        this.eco2 = eco2;
    }

    public Double getTvoc() {
        return tvoc;
    }

    public void setTvoc(Double tvoc) {
        this.tvoc = tvoc;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Nodo getNodo() {
        return nodo;
    }

    public void setNodo(Nodo nodo) {
        this.nodo = nodo;
    }


}