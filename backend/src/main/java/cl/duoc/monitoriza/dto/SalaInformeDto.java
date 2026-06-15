package cl.duoc.monitoriza.dto;

public class SalaInformeDto {

    private String nombre;
    private Double m2;
    private int cantidadEstudiantes;
    private int cantidadVentanas;
    private String aireAcondicionado;
    private String tipoDeVentilacion;
    private int numeroPiso;
    private String descripcionVentilacion;

    public SalaInformeDto() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getM2() { return m2; }
    public void setM2(Double m2) { this.m2 = m2; }

    public int getCantidadEstudiantes() { return cantidadEstudiantes; }
    public void setCantidadEstudiantes(int cantidadEstudiantes) { this.cantidadEstudiantes = cantidadEstudiantes; }

    public int getCantidadVentanas() { return cantidadVentanas; }
    public void setCantidadVentanas(int cantidadVentanas) { this.cantidadVentanas = cantidadVentanas; }

    public String getAireAcondicionado() { return aireAcondicionado; }
    public void setAireAcondicionado(String aireAcondicionado) { this.aireAcondicionado = aireAcondicionado; }

    public String getTipoDeVentilacion() { return tipoDeVentilacion; }
    public void setTipoDeVentilacion(String tipoDeVentilacion) { this.tipoDeVentilacion = tipoDeVentilacion; }

    public int getNumeroPiso() { return numeroPiso; }
    public void setNumeroPiso(int numeroPiso) { this.numeroPiso = numeroPiso; }

    public String getDescripcionVentilacion() { return descripcionVentilacion; }
    public void setDescripcionVentilacion(String descripcionVentilacion) { this.descripcionVentilacion = descripcionVentilacion; }
}
