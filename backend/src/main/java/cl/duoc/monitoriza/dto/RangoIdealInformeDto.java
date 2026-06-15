package cl.duoc.monitoriza.dto;

public class RangoIdealInformeDto {

    private String sensor;
    private String rango;
    private String unidad;

    public RangoIdealInformeDto() {}

    public RangoIdealInformeDto(String sensor, String rango, String unidad) {
        this.sensor = sensor;
        this.rango = rango;
        this.unidad = unidad;
    }

    public String getSensor() { return sensor; }
    public void setSensor(String sensor) { this.sensor = sensor; }

    public String getRango() { return rango; }
    public void setRango(String rango) { this.rango = rango; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
}
