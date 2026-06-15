package cl.duoc.monitoriza.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.monitoriza.dto.InformeDetalleDto;
import cl.duoc.monitoriza.dto.InformeListadoDto;
import cl.duoc.monitoriza.service.InformeService;

@RestController
@RequestMapping("/api/informes")
@CrossOrigin(origins = "http://localhost:5173")
public class InformeController {

    private final InformeService informeService;

    public InformeController(InformeService informeService) {
        this.informeService = informeService;
    }

    @GetMapping
    public List<InformeListadoDto> listar() {
        return informeService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(informeService.buscarPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/generar")
    public ResponseEntity<?> generar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            InformeDetalleDto informe = informeService.generarInforme(fecha);
            return ResponseEntity.ok(informe);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> descargarPdf(@PathVariable Long id) {
        try {
            byte[] pdf = informeService.obtenerPdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "informe-" + id + ".pdf");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}