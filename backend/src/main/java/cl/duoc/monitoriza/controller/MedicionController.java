package cl.duoc.monitoriza.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.monitoriza.model.Medicion;
import cl.duoc.monitoriza.service.MedicionService;

@RestController
@RequestMapping("/api/mediciones")
@CrossOrigin(origins = "http://localhost:5173")
public class MedicionController {


    @Autowired
    private MedicionService medicionService;

    @GetMapping
    public List<Medicion> listar(){
        return medicionService.listar();
    }

    @PostMapping
    public Medicion crear(@RequestBody Medicion medicion){
        return medicionService.crear(medicion);
    }
    
    @GetMapping("/ultimas")
    public List<Medicion> ultimas20(){
        return medicionService.ultimas20();
    }
    
}
