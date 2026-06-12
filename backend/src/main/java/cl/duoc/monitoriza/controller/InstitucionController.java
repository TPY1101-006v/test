package cl.duoc.monitoriza.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.monitoriza.model.Institucion;
import cl.duoc.monitoriza.service.InstitucionService;

@RestController
@RequestMapping("/api/instituciones")
@CrossOrigin(origins="http://localhost:5173")
public class InstitucionController {

    @Autowired
    private InstitucionService institucionService;

   @GetMapping
   public List<Institucion> listar(){
    return institucionService.listar();
   }

   @PostMapping
   public Institucion crear(@RequestBody Institucion institucion){
    return institucionService.crear(institucion);
   }

}
