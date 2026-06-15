package cl.duoc.monitoriza.dto;

import java.util.ArrayList;
import java.util.List;

public class GeminiInformeResponseDto {

    private List<AlteracionSensorDto> seccionAnalisis = new ArrayList<>();
    private String contextoSala;
    private List<SolucionDetalleDto> solucionesCortoPlazo = new ArrayList<>();
    private List<SolucionDetalleDto> solucionesLargoPlazo = new ArrayList<>();
    /** Compatibilidad con respuestas antiguas */
    private String solucionCortoPlazo;
    private String solucionLargoPlazo;

    public GeminiInformeResponseDto() {}

    public List<AlteracionSensorDto> getSeccionAnalisis() { return seccionAnalisis; }
    public void setSeccionAnalisis(List<AlteracionSensorDto> seccionAnalisis) { this.seccionAnalisis = seccionAnalisis; }

    public String getContextoSala() { return contextoSala; }
    public void setContextoSala(String contextoSala) { this.contextoSala = contextoSala; }

    public List<SolucionDetalleDto> getSolucionesCortoPlazo() { return solucionesCortoPlazo; }
    public void setSolucionesCortoPlazo(List<SolucionDetalleDto> solucionesCortoPlazo) {
        this.solucionesCortoPlazo = solucionesCortoPlazo;
    }

    public List<SolucionDetalleDto> getSolucionesLargoPlazo() { return solucionesLargoPlazo; }
    public void setSolucionesLargoPlazo(List<SolucionDetalleDto> solucionesLargoPlazo) {
        this.solucionesLargoPlazo = solucionesLargoPlazo;
    }

    public String getSolucionCortoPlazo() { return solucionCortoPlazo; }
    public void setSolucionCortoPlazo(String solucionCortoPlazo) { this.solucionCortoPlazo = solucionCortoPlazo; }

    public String getSolucionLargoPlazo() { return solucionLargoPlazo; }
    public void setSolucionLargoPlazo(String solucionLargoPlazo) { this.solucionLargoPlazo = solucionLargoPlazo; }
}
