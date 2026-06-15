package cl.duoc.monitoriza.dto;

public class LecturaInformeDto {

    private String hora;
    private String temperatura;
    private String humedad;
    private String db;
    private String lux;
    private String eco2;
    private String tvoc;
    private boolean temperaturaAlterada;
    private boolean humedadAlterada;
    private boolean dbAlterada;
    private boolean luxAlterada;
    private boolean eco2Alterada;
    private boolean tvocAlterada;

    public LecturaInformeDto() {}

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getTemperatura() { return temperatura; }
    public void setTemperatura(String temperatura) { this.temperatura = temperatura; }

    public String getHumedad() { return humedad; }
    public void setHumedad(String humedad) { this.humedad = humedad; }

    public String getDb() { return db; }
    public void setDb(String db) { this.db = db; }

    public String getLux() { return lux; }
    public void setLux(String lux) { this.lux = lux; }

    public String getEco2() { return eco2; }
    public void setEco2(String eco2) { this.eco2 = eco2; }

    public String getTvoc() { return tvoc; }
    public void setTvoc(String tvoc) { this.tvoc = tvoc; }

    public boolean isTemperaturaAlterada() { return temperaturaAlterada; }
    public void setTemperaturaAlterada(boolean temperaturaAlterada) { this.temperaturaAlterada = temperaturaAlterada; }

    public boolean isHumedadAlterada() { return humedadAlterada; }
    public void setHumedadAlterada(boolean humedadAlterada) { this.humedadAlterada = humedadAlterada; }

    public boolean isDbAlterada() { return dbAlterada; }
    public void setDbAlterada(boolean dbAlterada) { this.dbAlterada = dbAlterada; }

    public boolean isLuxAlterada() { return luxAlterada; }
    public void setLuxAlterada(boolean luxAlterada) { this.luxAlterada = luxAlterada; }

    public boolean isEco2Alterada() { return eco2Alterada; }
    public void setEco2Alterada(boolean eco2Alterada) { this.eco2Alterada = eco2Alterada; }

    public boolean isTvocAlterada() { return tvocAlterada; }
    public void setTvocAlterada(boolean tvocAlterada) { this.tvocAlterada = tvocAlterada; }
}
