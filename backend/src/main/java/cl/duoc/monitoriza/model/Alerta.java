package cl.duoc.monitoriza.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "alertas")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String sensor;
    private Double value;
    private String unit;
    private boolean high;
    private String time;

    // Constructor vacío obligatorio para JPA
    public Alerta() {
        // Formateamos la hora automáticamente al crear el registro al estilo Chile
        this.time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm:ss"));
    }

    public Alerta(String sensor, Double value, String unit, boolean high) {
        this();
        this.sensor = sensor;
        this.value = value;
        this.unit = unit;
        this.high = high;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSensor() { return sensor; }
    public void setSensor(String sensor) { this.sensor = sensor; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public boolean isHigh() { return high; }
    public void setHigh(boolean high) { this.high = high; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}