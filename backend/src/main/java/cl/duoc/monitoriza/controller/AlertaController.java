package cl.duoc.monitoriza.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.monitoriza.model.Alerta;
import cl.duoc.monitoriza.repository.AlertaRepository;

@RestController
@RequestMapping("/api/alertas")
@CrossOrigin(origins = "http://localhost:5173") // El puerto de tu React con Vite
public class AlertaController {

    @Autowired
    private AlertaRepository alertaRepository;

    // El Frontend llamará aquí para rellenar el botón de hamburguesa
    @GetMapping
    public List<Alerta> listarAlertas() {
        return alertaRepository.findTop15ByOrderByIdDesc();
    }

    // El Frontend (o el validador del ESP8266) llamará aquí para guardar un nuevo incidente
    @PostMapping
    public Alerta registrarAlerta(@RequestBody Alerta alerta) {
        return alertaRepository.save(alerta);
    }
}