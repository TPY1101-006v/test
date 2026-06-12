package cl.duoc.monitoriza.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.monitoriza.model.Sala;
import cl.duoc.monitoriza.service.SalaService;

@RestController
@RequestMapping("/api/salas")
@CrossOrigin(origins = "http://localhost:5173")
public class SalaController {

    @Autowired
    private SalaService salaService;

    @GetMapping
    public List<Sala> listar(){
        return salaService.listar();
    }

    @PostMapping
    public Sala crear(@RequestBody Sala sala){
        return salaService.crear(sala);
    }

    @PutMapping("/{idSala}")
    public Sala actualizar(@PathVariable Long idSala, @RequestBody Sala sala){
        return salaService.actualizar(idSala, sala);
    }

}
