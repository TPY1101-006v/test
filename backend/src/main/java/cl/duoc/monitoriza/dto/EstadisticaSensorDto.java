package cl.duoc.monitoriza.dto;

public class EstadisticaSensorDto {
    private String sensor;
    private String unidad;
    private Double minimo;
    private Double maximo;
    private Double promedio;

    public EstadisticaSensorDto() {}

    public String getSensor() { return sensor; }
    public void setSensor(String sensor) { this.sensor = sensor; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public Double getMinimo() { return minimo; }
    public void setMinimo(Double minimo) { this.minimo = minimo; }

    public Double getMaximo() { return maximo; }
    public void setMaximo(Double maximo) { this.maximo = maximo; }

    public Double getPromedio() { return promedio; }
    public void setPromedio(Double promedio) { this.promedio = promedio; }
}
