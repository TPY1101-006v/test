package cl.duoc.monitoriza.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ResumenDiaDto {
    private LocalDate fecha;
    private boolean diaHabil;
    private String mensaje;
    private int totalLecturasJornada;
    private int totalLecturasClase;
    private SalaInformeDto sala;
    private List<EstadisticaSensorDto> estadisticas = new ArrayList<>();
    private List<BloqueMedicionDto> bloques = new ArrayList<>();
    /** Conteo por bloque de clase (legacy / referencia) */
    private List<AlteracionSensorDto> alteraciones = new ArrayList<>();
    /** Conteo por lectura individual en toda la jornada 7:30-16:30 */
    private List<AlteracionSensorDto> alteracionesJornada = new ArrayList<>();

    public ResumenDiaDto() {}

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public boolean isDiaHabil() { return diaHabil; }
    public void setDiaHabil(boolean diaHabil) { this.diaHabil = diaHabil; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public int getTotalLecturasJornada() { return totalLecturasJornada; }
    public void setTotalLecturasJornada(int totalLecturasJornada) { this.totalLecturasJornada = totalLecturasJornada; }

    public int getTotalLecturasClase() { return totalLecturasClase; }
    public void setTotalLecturasClase(int totalLecturasClase) { this.totalLecturasClase = totalLecturasClase; }

    public SalaInformeDto getSala() { return sala; }
    public void setSala(SalaInformeDto sala) { this.sala = sala; }

    public List<EstadisticaSensorDto> getEstadisticas() { return estadisticas; }
    public void setEstadisticas(List<EstadisticaSensorDto> estadisticas) { this.estadisticas = estadisticas; }

    public List<BloqueMedicionDto> getBloques() { return bloques; }
    public void setBloques(List<BloqueMedicionDto> bloques) { this.bloques = bloques; }

    public List<AlteracionSensorDto> getAlteraciones() { return alteraciones; }
    public void setAlteraciones(List<AlteracionSensorDto> alteraciones) { this.alteraciones = alteraciones; }

    public List<AlteracionSensorDto> getAlteracionesJornada() { return alteracionesJornada; }
    public void setAlteracionesJornada(List<AlteracionSensorDto> alteracionesJornada) {
        this.alteracionesJornada = alteracionesJornada;
    }
}
