package cl.duoc.monitoriza.controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.monitoriza.dto.ReportMetadataDto;
import cl.duoc.monitoriza.model.Medicion;
import cl.duoc.monitoriza.model.Nodo;
import cl.duoc.monitoriza.model.Sala;
import cl.duoc.monitoriza.repository.MedicionRepository;

@RestController
@RequestMapping("/api/report")
@CrossOrigin(origins = "http://localhost:5173")
public class ReportController {

    private static final DateTimeFormatter FECHA_CSV = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FECHA_META = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final char SEP = ';';

    private final MedicionRepository medicionRepository;

    public ReportController(MedicionRepository medicionRepository) {
        this.medicionRepository = medicionRepository;
    }

    @GetMapping("/info")
    public ReportMetadataDto obtenerMetadata() {
        long total = medicionRepository.count();
        Medicion primera = medicionRepository.findFirstByOrderByFechaHoraAsc();
        Medicion ultima = medicionRepository.findFirstByOrderByFechaHoraDesc();

        String desde = primera != null && primera.getFechaHora() != null
                ? primera.getFechaHora().format(FECHA_META) : null;
        String hasta = ultima != null && ultima.getFechaHora() != null
                ? ultima.getFechaHora().format(FECHA_META) : null;

        String descripcion = total == 0
                ? "No hay mediciones almacenadas en la base de datos."
                : "Exportacion completa: todas las lecturas guardadas (no solo las 20 del dashboard). "
                        + "Ordenadas de la mas antigua a la mas reciente.";

        return new ReportMetadataDto(total, desde, hasta, descripcion);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> descargarCSV() {
        try {
            List<Medicion> mediciones = medicionRepository.findAllByOrderByFechaHoraAsc();
            byte[] csv = construirCsv(mediciones);

            String fechaArchivo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=monitoriza-mediciones-" + fechaArchivo + ".csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                    .body(csv);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(("Error generando CSV: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    byte[] construirCsv(List<Medicion> mediciones) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8);

        LocalDateTime ahora = LocalDateTime.now();
        Medicion primera = mediciones.isEmpty() ? null : mediciones.get(0);
        Medicion ultima = mediciones.isEmpty() ? null : mediciones.get(mediciones.size() - 1);

        writer.println("# MONITORIZA - Exportacion de mediciones ambientales");
        writer.println("# Generado" + SEP + ahora.format(FECHA_CSV));
        writer.println("# Total de lecturas" + SEP + mediciones.size());
        if (primera != null && ultima != null && primera.getFechaHora() != null && ultima.getFechaHora() != null) {
            writer.println("# Periodo" + SEP + primera.getFechaHora().format(FECHA_CSV)
                    + " a " + ultima.getFechaHora().format(FECHA_CSV));
        }
        writer.println("# Alcance" + SEP + "Todas las mediciones almacenadas en MySQL (historico completo del sistema)");
        writer.println("# Nota" + SEP + "El dashboard muestra solo las 20 mas recientes; este archivo incluye el registro completo");
        writer.println("#");

        writer.println(String.join(String.valueOf(SEP),
                "Fecha y hora",
                "ID nodo",
                "Nombre nodo",
                "Sala",
                "Temperatura (C)",
                "Humedad (%)",
                "Ruido (dB)",
                "Iluminacion (lux)",
                "CO2 (ppm)",
                "TVOC (ppb)"));

        for (Medicion m : mediciones) {
            if (m.getNodo() == null) {
                continue;
            }
            Nodo nodo = m.getNodo();
            Sala sala = nodo.getSala();
            String fechaStr = m.getFechaHora() != null ? m.getFechaHora().format(FECHA_CSV) : "";
            String nombreNodo = nodo.getNombre() != null ? nodo.getNombre() : "";
            String nombreSala = sala != null && sala.getNombre() != null ? sala.getNombre() : "";

            writer.printf(Locale.US, "%s%c%d%c%s%c%s%c%.1f%c%.1f%c%.1f%c%.0f%c%.0f%c%.0f%n",
                    fechaStr, SEP,
                    nodo.getIdNodo(), SEP,
                    escaparCsv(nombreNodo), SEP,
                    escaparCsv(nombreSala), SEP,
                    nullSafe(m.getTemperatura()), SEP,
                    nullSafe(m.getHumedad()), SEP,
                    nullSafe(m.getDb()), SEP,
                    nullSafe(m.getLux()), SEP,
                    nullSafe(m.getEco2()), SEP,
                    nullSafe(m.getTvoc()));
        }

        writer.flush();
        byte[] raw = out.toByteArray();
        byte[] withBom = new byte[raw.length + 3];
        withBom[0] = (byte) 0xEF;
        withBom[1] = (byte) 0xBB;
        withBom[2] = (byte) 0xBF;
        System.arraycopy(raw, 0, withBom, 3, raw.length);
        return withBom;
    }

    private String escaparCsv(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(String.valueOf(SEP)) || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    private double nullSafe(Double v) {
        return v == null ? 0.0 : v;
    }
}
