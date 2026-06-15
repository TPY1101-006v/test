package cl.duoc.monitoriza.dto;

public class ReportMetadataDto {

    private long totalMediciones;
    private String fechaDesde;
    private String fechaHasta;
    private String descripcion;

    public ReportMetadataDto() {}

    public ReportMetadataDto(long totalMediciones, String fechaDesde, String fechaHasta, String descripcion) {
        this.totalMediciones = totalMediciones;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.descripcion = descripcion;
    }

    public long getTotalMediciones() {
        return totalMediciones;
    }

    public void setTotalMediciones(long totalMediciones) {
        this.totalMediciones = totalMediciones;
    }

    public String getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(String fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public String getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(String fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
