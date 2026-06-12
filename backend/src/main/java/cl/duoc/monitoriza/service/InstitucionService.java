package cl.duoc.monitoriza.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.duoc.monitoriza.model.Institucion;
import cl.duoc.monitoriza.repository.InstitucionRepository;

@Service
public class InstitucionService {

    @Autowired
    private InstitucionRepository institucionRepository;

    public List<Institucion> listar(){
        return institucionRepository.findAll();
    }

    public Institucion crear(Institucion institucion){
        institucionRepository.save(institucion);
        return institucion;
    }
}