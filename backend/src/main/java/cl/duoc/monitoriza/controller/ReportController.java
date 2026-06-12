package cl.duoc.monitoriza.controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.monitoriza.model.Medicion;
import cl.duoc.monitoriza.repository.MedicionRepository;


@RestController
@CrossOrigin(origins = "http://localhost:5173") // Ajusta al puerto de tu frontend
public class ReportController {

    private final MedicionRepository medicionRepository;

    public ReportController(MedicionRepository medicionRepository) {
        this.medicionRepository = medicionRepository;
    }

    @GetMapping("/api/report")
    public ResponseEntity<byte[]> descargarCSV() {
        try {
            List<Medicion> mediciones = medicionRepository.findAll();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(out);

            // Encabezado CSV
            writer.println("idNodo,descripcionNodo,temperatura,humedad,db,lux,eco2,tvoc,fecha");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            for (Medicion m : mediciones) {
                if (m.getNodo() == null) continue; // Evita nodos nulos
                String fechaStr = m.getFechaHora() != null ? m.getFechaHora().format(formatter) : "";
                writer.printf("%d,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%s\n",
                        m.getNodo().getIdNodo(),
                        m.getNodo().getDescripcion(),
                        m.getTemperatura(),
                        m.getHumedad(),
                        m.getDb(),
                        m.getLux(),
                        m.getEco2(),
                        m.getTvoc(),
                        fechaStr);
            }

            writer.flush();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=informe.csv");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(("Error generando CSV: " + e.getMessage()).getBytes());
        }
    }
}