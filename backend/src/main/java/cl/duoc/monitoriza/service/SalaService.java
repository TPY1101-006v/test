package cl.duoc.monitoriza.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.duoc.monitoriza.model.Sala;
import cl.duoc.monitoriza.repository.SalaRepository;

@Service
public class SalaService {

    @Autowired
    private SalaRepository salaRepository;

    public List<Sala> listar(){
        return salaRepository.findAll();
    }

    public Sala crear(Sala sala){
        salaRepository.save(sala);
        return sala;
    }

    public Sala actualizar(Long idSala, Sala sala){
        Sala salaExistente = salaRepository.findById(idSala).orElseThrow(() -> new RuntimeException("Sala no encontrada"));
        salaExistente.setM2(sala.getM2());
        salaExistente.setCantidadEstudiantes(sala.getCantidadEstudiantes());
        salaExistente.setCantidadVentanas(sala.getCantidadVentanas());
        salaExistente.setAireAcondicionado(sala.getAireAcondicionado());
        salaExistente.setTipoDeVentilacion(sala.getTipoDeVentilacion());
        salaExistente.setNumeroPiso(sala.getNumeroPiso());
        salaRepository.save(salaExistente);
        return salaExistente;
    }
}
