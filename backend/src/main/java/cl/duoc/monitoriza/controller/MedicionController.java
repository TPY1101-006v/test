package cl.duoc.monitoriza.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.monitoriza.dto.ResumenDiaDto;
import cl.duoc.monitoriza.model.Medicion;
import cl.duoc.monitoriza.service.MedicionService;
import cl.duoc.monitoriza.service.ResumenDiaService;
import cl.duoc.monitoriza.util.BloqueHorario;
import cl.duoc.monitoriza.util.HorarioEscolarUtil;

@RestController
@RequestMapping("/api/mediciones")
@CrossOrigin(origins = "http://localhost:5173")
public class MedicionController {

    @Autowired
    private ResumenDiaService resumenDiaService;    


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
    
    @GetMapping("/por-dia")
    public Map<String, Object> medicionesPorDia(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        var delDia = medicionService.medicionesDelDia(fecha);
        var enClase = medicionService.medicionesBloquesClaseDelDia(fecha);
        var agrupadas = HorarioEscolarUtil.agruparPorBloque(delDia);
        Map<String, Integer> conteoPorBloque = new LinkedHashMap<>();
        for (BloqueHorario bloque : BloqueHorario.values()) {
            conteoPorBloque.put(
                    bloque.getEtiqueta(),
                    agrupadas.getOrDefault(bloque, List.of()).size()
            );
    }
    Map<String, Object> respuesta = new LinkedHashMap<>();
    respuesta.put("fecha", fecha);
    respuesta.put("diaHabil", HorarioEscolarUtil.esDiaHabil(fecha));
    respuesta.put("totalJornada", delDia.size());
    respuesta.put("totalBloquesClase", enClase.size());
    respuesta.put("conteoPorBloque", conteoPorBloque);
    return respuesta;
    }

    @GetMapping("/resumen-dia")
    public ResumenDiaDto resumenDia(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
    return resumenDiaService.construirResumen(fecha);
}
}
