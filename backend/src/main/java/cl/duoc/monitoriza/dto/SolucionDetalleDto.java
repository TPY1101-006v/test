package cl.duoc.monitoriza.dto;

import java.util.ArrayList;
import java.util.List;

public class SolucionDetalleDto {

    private String titulo;
    private List<String> indicadores = new ArrayList<>();
    private String explicacion;

    public SolucionDetalleDto() {}

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public List<String> getIndicadores() { return indicadores; }
    public void setIndicadores(List<String> indicadores) { this.indicadores = indicadores; }

    public String getExplicacion() { return explicacion; }
    public void setExplicacion(String explicacion) { this.explicacion = explicacion; }
}
