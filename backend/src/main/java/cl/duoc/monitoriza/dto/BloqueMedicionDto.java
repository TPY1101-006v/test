package cl.duoc.monitoriza.dto;

public class BloqueMedicionDto {
    private String nombre;
    private String tituloAmigable;
    private String horario;
    private boolean bloqueDeClase;
    private int cantidadLecturas;
    private java.util.List<LecturaInformeDto> lecturas = new java.util.ArrayList<>();
    // Valores promedio formateados (6 campos de Medicion)
    private String temperatura;
    private String humedad;
    private String db;
    private String lux;
    private String eco2;
    private String tvoc;
    // Flags para pintar rojo en PDF (6 flags)
    private boolean temperaturaAlterada;
    private boolean humedadAlterada;
    private boolean dbAlterada;
    private boolean luxAlterada;
    private boolean eco2Alterada;
    private boolean tvocAlterada;

    public BloqueMedicionDto() {
    }

    public BloqueMedicionDto(String nombre, boolean bloqueDeClase, int cantidadLecturas, String temperatura, String humedad, String db, String lux, String eco2, String tvoc, boolean temperaturaAlterada, boolean humedadAlterada, boolean dbAlterada, boolean luxAlterada, boolean eco2Alterada, boolean tvocAlterada) {
        this.nombre = nombre;
        this.bloqueDeClase = bloqueDeClase;
        this.cantidadLecturas = cantidadLecturas;
        this.temperatura = temperatura;
        this.humedad = humedad;
        this.db = db;
        this.lux = lux;
        this.eco2 = eco2;
        this.tvoc = tvoc;
        this.temperaturaAlterada = temperaturaAlterada;
        this.humedadAlterada = humedadAlterada;
        this.dbAlterada = dbAlterada;
        this.luxAlterada = luxAlterada;
        this.eco2Alterada = eco2Alterada;
        this.tvocAlterada = tvocAlterada;
    }
    
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getTituloAmigable() {
        return tituloAmigable;
    }
    public void setTituloAmigable(String tituloAmigable) {
        this.tituloAmigable = tituloAmigable;
    }
    public String getHorario() {
        return horario;
    }
    public void setHorario(String horario) {
        this.horario = horario;
    }
    public java.util.List<LecturaInformeDto> getLecturas() {
        return lecturas;
    }
    public void setLecturas(java.util.List<LecturaInformeDto> lecturas) {
        this.lecturas = lecturas;
    }
    public boolean isBloqueDeClase() {
        return bloqueDeClase;
    }
    public void setBloqueDeClase(boolean bloqueDeClase) {
        this.bloqueDeClase = bloqueDeClase;
    }
    public int getCantidadLecturas() {
        return cantidadLecturas;
    }
    public void setCantidadLecturas(int cantidadLecturas) {
        this.cantidadLecturas = cantidadLecturas;
    }
    public String getTemperatura() {
        return temperatura;
    }
    public void setTemperatura(String temperatura) {
        this.temperatura = temperatura;
    }
    public String getHumedad() {
        return humedad;
    }
    public void setHumedad(String humedad) {
        this.humedad = humedad;
    }
    public String getDb() {
        return db;
    }
    public void setDb(String db) {
        this.db = db;
    }
    public String getLux() {
        return lux;
    }
    public void setLux(String lux) {
        this.lux = lux;
    }
    public String getEco2() {
        return eco2;
    }
    public void setEco2(String eco2) {
        this.eco2 = eco2;
    }
    public String getTvoc() {
        return tvoc;
    }
    public void setTvoc(String tvoc) {
        this.tvoc = tvoc;
    }
    public boolean isTemperaturaAlterada() {
        return temperaturaAlterada;
    }
    public void setTemperaturaAlterada(boolean temperaturaAlterada) {
        this.temperaturaAlterada = temperaturaAlterada;
    }
    public boolean isHumedadAlterada() {
        return humedadAlterada;
    }
    public void setHumedadAlterada(boolean humedadAlterada) {
        this.humedadAlterada = humedadAlterada;
    }
    public boolean isDbAlterada() {
        return dbAlterada;
    }
    public void setDbAlterada(boolean dbAlterada) {
        this.dbAlterada = dbAlterada;
    }
    public boolean isLuxAlterada() {
        return luxAlterada;
    }
    public void setLuxAlterada(boolean luxAlterada) {
        this.luxAlterada = luxAlterada;
    }
    public boolean isEco2Alterada() {
        return eco2Alterada;
    }
    public void setEco2Alterada(boolean eco2Alterada) {
        this.eco2Alterada = eco2Alterada;
    }
    public boolean isTvocAlterada() {
        return tvocAlterada;
    }
    public void setTvocAlterada(boolean tvocAlterada) {
        this.tvocAlterada = tvocAlterada;
    }
}
