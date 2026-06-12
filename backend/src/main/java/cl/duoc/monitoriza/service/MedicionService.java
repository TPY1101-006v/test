package cl.duoc.monitoriza.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.duoc.monitoriza.model.Medicion;
import cl.duoc.monitoriza.repository.MedicionRepository;

//aca esta la logica del negocio
//en la primera version, no habia service

@Service
public class MedicionService {
     
    @Autowired
    private MedicionRepository medicionRepository;

    //pide una lista de todas las mediciones
    public List<Medicion> listar(){
        return medicionRepository.findAll();
    }
    
    //lista las ultimas 20 mediciones
    public List<Medicion> ultimas20() {
        return medicionRepository.findTop20ByOrderByFechaHoraDesc();
    }

    public Medicion crear(Medicion medicion){
        medicionRepository.save(medicion);
        return medicion;
    }
}