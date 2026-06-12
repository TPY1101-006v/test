package cl.duoc.monitoriza.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Nodo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNodo;

    private String nombre;
    private String descripcion;

    @ManyToOne
    @JoinColumn(name="sala_id")
    @JsonBackReference
    private Sala sala;

    public Nodo(){
    }

    public Nodo(String nombre, String descripcion, Sala sala) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.sala = sala;
    }

    public Nodo(Long idNodo, String nombre, String descripcion, Sala sala) {
        this.idNodo = idNodo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.sala = sala;
    }

    public Long getIdNodo() {
        return idNodo;
    }

    public void setIdNodo(Long idNodo) {
        this.idNodo = idNodo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
    }
    
}
