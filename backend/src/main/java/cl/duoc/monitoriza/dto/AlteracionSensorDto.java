package cl.duoc.monitoriza.dto;

public class AlteracionSensorDto {
    private String sensor;
    private int vecesAlterada;
    private String consecuencia;

    public AlteracionSensorDto() {}

    public String getSensor() { return sensor; }
    public void setSensor(String sensor) { this.sensor = sensor; }

    public int getVecesAlterada() { return vecesAlterada; }
    public void setVecesAlterada(int vecesAlterada) { this.vecesAlterada = vecesAlterada; }

    public String getConsecuencia() { return consecuencia; }
    public void setConsecuencia(String consecuencia) { this.consecuencia = consecuencia; }
}