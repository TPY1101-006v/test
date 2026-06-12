package cl.duoc.monitoriza.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Institucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInstitucion;

    private String nombre;
    private String direccion;

    public Institucion(){
    }

    public Institucion(String nombre, String direccion) {
        this.nombre = nombre;
        this.direccion = direccion;
    }

    public Institucion(Long idInstitucion, String nombre, String direccion) {
        this.idInstitucion = idInstitucion;
        this.nombre = nombre;
        this.direccion = direccion;
    }

    public Long getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(Long idInstitucion) {
        this.idInstitucion = idInstitucion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    
}
