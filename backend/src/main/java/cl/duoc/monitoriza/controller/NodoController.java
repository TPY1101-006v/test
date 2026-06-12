package cl.duoc.monitoriza.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.monitoriza.model.Nodo;
import cl.duoc.monitoriza.service.NodoService;

@RestController
@RequestMapping("/api/nodos")
@CrossOrigin(origins = "http://localhost:5173")

public class NodoController {

    @Autowired
    private NodoService nodoService;

    @GetMapping
    public List<Nodo> listar(){
        return nodoService.listar();
    }

    @PostMapping
    public Nodo crear(@RequestBody Nodo nodo){
        return nodoService.crear(nodo);
    }
}
