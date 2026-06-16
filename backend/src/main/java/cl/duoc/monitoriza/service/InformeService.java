package cl.duoc.monitoriza.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.duoc.monitoriza.dto.GeminiInformeResponseDto;
import cl.duoc.monitoriza.dto.InformeDetalleDto;
import cl.duoc.monitoriza.dto.InformeListadoDto;
import cl.duoc.monitoriza.dto.ResumenDiaDto;
import cl.duoc.monitoriza.model.Informe;
import cl.duoc.monitoriza.repository.InformeRepository;
import cl.duoc.monitoriza.util.HorarioEscolarUtil;

@Service
public class InformeService {

    private static final Logger log = LoggerFactory.getLogger(InformeService.class);

    private final InformeRepository informeRepository;
    private final ResumenDiaService resumenDiaService;
    private final MedicionService medicionService;
    private final GeminiService geminiService;
    private final InformePdfService informePdfService;
    private final ObjectMapper objectMapper;

    @Value("${app.informe.seed.pausa-ms:6000}")
    private long pausaEntreInformesMs;

    public InformeService(
            InformeRepository informeRepository,
            ResumenDiaService resumenDiaService,
            MedicionService medicionService,
            GeminiService geminiService,
            InformePdfService informePdfService,
            ObjectMapper objectMapper) {
        this.informeRepository = informeRepository;
        this.resumenDiaService = resumenDiaService;
        this.medicionService = medicionService;
        this.geminiService = geminiService;
        this.informePdfService = informePdfService;
        this.objectMapper = objectMapper;
    }

    public List<InformeListadoDto> listar() {
        return informeRepository.findAllByOrderByFechaDesc()
                .stream()
                .map(this::toListadoDto)
                .toList();
    }

    public InformeDetalleDto buscarPorId(Long id) {
        Informe informe = informeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Informe no encontrado: " + id));
        return toDetalleDto(informe);
    }

    public byte[] obtenerPdf(Long id) {
        Informe informe = informeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Informe no encontrado: " + id));

        byte[] pdf = informe.getPdfGenerado();
        if (pdf == null || pdf.length == 0) {
            throw new IllegalStateException("El informe aún no tiene PDF generado");
        }
        return pdf;
    }

    public InformeDetalleDto generarInforme(LocalDate fecha) {
        if (!HorarioEscolarUtil.esDiaHabil(fecha)) {
            throw new IllegalArgumentException("Solo se generan informes de lunes a viernes: " + fecha);
        }
        if (informeRepository.existsByFecha(fecha)) {
            throw new IllegalStateException("Ya existe un informe para la fecha: " + fecha);
        }

        ResumenDiaDto resumen = resumenDiaService.construirResumen(fecha);
        GeminiInformeResponseDto analisis = geminiService.generarAnalisis(resumen);

        Informe informe = new Informe();
        informe.setFecha(fecha);
        informe.setContenidoJson(toJson(resumen));
        informe.setAnalisisJson(toJson(analisis));
        informe.setPdfGenerado(informePdfService.generarPdf(resumen, analisis));
        informe.setFechaGeneracion(LocalDateTime.now());

        Informe guardado = informeRepository.save(informe);
        return toDetalleDto(guardado);
    }

    /**
     * Genera informes para todos los días hábiles del rango que tengan mediciones
     * en jornada escolar (7:30–16:30) y aún no tengan informe.
     */
    public int generarInformesPendientes(LocalDate desde, LocalDate hasta) {
        if (!geminiService.tieneApiKeyConfigurada()) {
            ResultadoGeneracionSeed resultado = new ResultadoGeneracionSeed(
                    0, 0, 0, 0, 0, true,
                    "GEMINI_API_KEY no configurada. Define la variable de entorno antes de iniciar la app.");
            imprimirBannerFinGeneracion(resultado, desde, hasta);
            return 0;
        }

        int generados = 0;
        int fallidos = 0;
        int omitidosYaExisten = 0;
        int omitidosSinMediciones = 0;
        int diasPendientes = 0;
        boolean falloApiKey = false;
        String detalleError = null;

        for (LocalDate dia = desde; !dia.isAfter(hasta); dia = dia.plusDays(1)) {
            if (!HorarioEscolarUtil.esDiaHabil(dia)) {
                continue;
            }
            if (informeRepository.existsByFecha(dia)) {
                omitidosYaExisten++;
                continue;
            }
            if (medicionService.medicionesDelDia(dia).isEmpty()) {
                omitidosSinMediciones++;
                continue;
            }

            diasPendientes++;

            try {
                generarInforme(dia);
                generados++;
                log.info("[InformeSeed] Informe generado para {}", dia);
                pausarEntreInformes();
            } catch (IllegalStateException e) {
                if (informeRepository.existsByFecha(dia)) {
                    omitidosYaExisten++;
                    log.debug("[InformeSeed] Informe ya existente para {}: {}", dia, e.getMessage());
                } else {
                    fallidos++;
                    detalleError = e.getMessage();
                    log.error("[InformeSeed] Error generando informe para {}: {}", dia, e.getMessage(), e);
                    if (GeminiService.esErrorDeAutenticacion(e)) {
                        falloApiKey = true;
                        break;
                    }
                }
            } catch (Exception e) {
                fallidos++;
                detalleError = e.getMessage();
                log.error("[InformeSeed] Error generando informe para {}: {}", dia, e.getMessage(), e);
                if (GeminiService.esErrorDeAutenticacion(e)) {
                    falloApiKey = true;
                    break;
                }
            }
        }

        ResultadoGeneracionSeed resultado = new ResultadoGeneracionSeed(
                generados, fallidos, omitidosYaExisten, omitidosSinMediciones,
                diasPendientes, falloApiKey, detalleError);
        imprimirBannerFinGeneracion(resultado, desde, hasta);
        return generados;
    }

    private void imprimirBannerFinGeneracion(ResultadoGeneracionSeed resultado, LocalDate desde, LocalDate hasta) {
        long totalEnBd = informeRepository.count();
        String estado = determinarEstado(resultado);
        String separador = "=".repeat(64);

        System.out.println();
        System.out.println(separador);
        System.out.println("  *** GENERACION DE INFORMES HISTORICOS ***");
        System.out.println(separador);
        System.out.println("  ESTADO: " + estado);
        System.out.println("  Informes nuevos generados: " + resultado.generados());
        System.out.println("  Intentos fallidos: " + resultado.fallidos());
        System.out.println("  Dias omitidos (ya existian): " + resultado.omitidosYaExisten());
        System.out.println("  Dias omitidos (sin mediciones): " + resultado.omitidosSinMediciones());
        System.out.println("  Dias pendientes procesados: " + resultado.diasPendientes());
        System.out.println("  Total informes en base de datos: " + totalEnBd);
        System.out.println("  Rango procesado: " + desde + " -> " + hasta);

        if (resultado.falloApiKey()) {
            System.out.println();
            System.out.println("  *** ERROR: GEMINI_API_KEY NO CONFIGURADA O INVALIDA ***");
            System.out.println("  Los informes con IA requieren una API key valida de Google Gemini.");
            if (resultado.detalleError() != null && !resultado.detalleError().isBlank()) {
                System.out.println("  Detalle: " + resultado.detalleError());
            }
            System.out.println("  Configurala antes de iniciar el backend:");
            System.out.println("    PowerShell: $env:GEMINI_API_KEY = \"tu-clave\"");
            System.out.println("    Obtener clave: https://aistudio.google.com/apikey");
        } else if (resultado.fallidos() > 0 && resultado.detalleError() != null) {
            System.out.println();
            System.out.println("  Ultimo error detectado: " + resultado.detalleError());
        }

        System.out.println(separador);
        System.out.println();

        log.info("[InformeSeed] Estado={}, generados={}, fallidos={}, totalBD={}, rango {} -> {}, apiKeyError={}",
                estado, resultado.generados(), resultado.fallidos(), totalEnBd, desde, hasta, resultado.falloApiKey());
    }

    private String determinarEstado(ResultadoGeneracionSeed resultado) {
        if (resultado.falloApiKey()) {
            return "FALLO_API_KEY";
        }
        if (resultado.diasPendientes() == 0) {
            return "SIN_PENDIENTES";
        }
        if (resultado.generados() == 0 && resultado.fallidos() > 0) {
            return "FALLO";
        }
        if (resultado.generados() > 0 && resultado.fallidos() > 0) {
            return "PARCIAL";
        }
        if (resultado.generados() > 0) {
            return "EXITO";
        }
        return "SIN_CAMBIOS";
    }

    private record ResultadoGeneracionSeed(
            int generados,
            int fallidos,
            int omitidosYaExisten,
            int omitidosSinMediciones,
            int diasPendientes,
            boolean falloApiKey,
            String detalleError) {}

    private void pausarEntreInformes() {
        if (pausaEntreInformesMs <= 0) {
            return;
        }
        try {
            Thread.sleep(pausaEntreInformesMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[InformeSeed] Pausa entre informes interrumpida");
        }
    }

    private String toJson(Object objeto) {
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el informe a JSON", e);
        }
    }

    private InformeListadoDto toListadoDto(Informe informe) {
        InformeListadoDto dto = new InformeListadoDto();
        dto.setId(informe.getId());
        dto.setFecha(informe.getFecha());
        dto.setFechaGeneracion(informe.getFechaGeneracion());
        dto.setTienePdf(tienePdf(informe));
        dto.setTieneAnalisis(informe.getAnalisisJson() != null && !informe.getAnalisisJson().isBlank());
        return dto;
    }

    private InformeDetalleDto toDetalleDto(Informe informe) {
        InformeDetalleDto dto = new InformeDetalleDto();
        dto.setId(informe.getId());
        dto.setFecha(informe.getFecha());
        dto.setContenidoJson(informe.getContenidoJson());
        dto.setAnalisisJson(informe.getAnalisisJson());
        dto.setFechaGeneracion(informe.getFechaGeneracion());
        dto.setTienePdf(tienePdf(informe));
        return dto;
    }

    private boolean tienePdf(Informe informe) {
        byte[] pdf = informe.getPdfGenerado();
        return pdf != null && pdf.length > 0;
    }
}