package cl.duoc.monitoriza.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.duoc.monitoriza.model.Nodo;
import cl.duoc.monitoriza.repository.NodoRepository;

@Service
public class NodoService {
    
    @Autowired
    private NodoRepository nodoRepository;

    //devuelve lista con nodos
    public List<Nodo> listar(){
        return nodoRepository.findAll();
    }

    //crea un nodo
    public Nodo crear(Nodo nodo){
        nodoRepository.save(nodo);
        return nodo;
    }
}
