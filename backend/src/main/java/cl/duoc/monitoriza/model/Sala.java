package cl.duoc.monitoriza.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSala;

    private String nombre;
    private Double m2;
    private int cantidadEstudiantes;
    private int cantidadVentanas;
    private String aireAcondicionado;
    private String tipoDeVentilacion;
    private int numeroPiso;

    @ManyToOne
    @JoinColumn(name="institucion_id")
    @JsonBackReference
    private Institucion institucion;

    public Sala() {
    }

    
    public Sala(String nombre, Double m2, int cantidadEstudiantes, int cantidadVentanas, 
        Institucion institucion, String aireAcondicionado, String tipoDeVentilacion, int numeroPiso) {
    this.nombre = nombre;
    this.m2 = m2;
    this.cantidadEstudiantes = cantidadEstudiantes;
    this.cantidadVentanas = cantidadVentanas;
    this.institucion = institucion;
    this.aireAcondicionado = aireAcondicionado;
    this.tipoDeVentilacion = tipoDeVentilacion;
    this.numeroPiso = numeroPiso;
}


    public Sala(Long idSala, String nombre, Double m2, int cantidadEstudiantes, int cantidadVentanas, 
            Institucion institucion, String aireAcondicionado, String tipoDeVentilacion, int numeroPiso) {
        this.idSala = idSala;
        this.nombre = nombre;
        this.m2 = m2;
        this.cantidadEstudiantes = cantidadEstudiantes;
        this.cantidadVentanas = cantidadVentanas;
        this.institucion = institucion;
        this.aireAcondicionado = aireAcondicionado;
        this.tipoDeVentilacion = tipoDeVentilacion;
        this.numeroPiso = numeroPiso;
    }



    public Long getIdSala() {
        return idSala;
    }

    public void setIdSala(Long idSala) {
        this.idSala = idSala;
    }

    public Double getM2() {
        return m2;
    }

    public void setM2(Double m2) {
        this.m2 = m2;
    }

    public int getCantidadEstudiantes() {
        return cantidadEstudiantes;
    }

    public void setCantidadEstudiantes(int cantidadEstudiantes) {
        this.cantidadEstudiantes = cantidadEstudiantes;
    }

    public int getCantidadVentanas() {
        return cantidadVentanas;
    }

    public void setCantidadVentanas(int cantidadVentanas) {
        this.cantidadVentanas = cantidadVentanas;
    }

    public Institucion getInstitucion() {
        return institucion;
    }

    public void setInstitucion(Institucion institucion) {
        this.institucion = institucion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getAireAcondicionado() {
        return aireAcondicionado;
    }
    public void setAireAcondicionado(String aireAcondicionado) {
        this.aireAcondicionado = aireAcondicionado;
    }
    public String getTipoDeVentilacion() {
        return tipoDeVentilacion;
    }
    public void setTipoDeVentilacion(String tipoDeVentilacion) {
        this.tipoDeVentilacion = tipoDeVentilacion;
    }
    public int getNumeroPiso() {
        return numeroPiso;
    }

    public void setNumeroPiso(int numeroPiso) {
        this.numeroPiso = numeroPiso;
    }


}
