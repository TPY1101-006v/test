package cl.duoc.monitoriza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import cl.duoc.monitoriza.dto.AlteracionSensorDto;
import cl.duoc.monitoriza.dto.BloqueMedicionDto;
import cl.duoc.monitoriza.dto.GeminiInformeResponseDto;
import cl.duoc.monitoriza.dto.LecturaInformeDto;
import cl.duoc.monitoriza.dto.ResumenDiaDto;
import cl.duoc.monitoriza.dto.SalaInformeDto;
import cl.duoc.monitoriza.dto.SolucionDetalleDto;

class InformePdfServiceTest {

    private InformePdfService informePdfService;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        informePdfService = new InformePdfService(engine);
    }

    @Test
    void exportarPdfMuestraInstitucional() throws Exception {
        ResumenDiaDto resumen = resumenEjemploCompleto();
        GeminiInformeResponseDto analisis = analisisEjemploCompleto();

        byte[] pdf = informePdfService.generarPdf(resumen, analisis);

        Path destinoRaiz = Path.of("..", "informe-muestra-monitoriza.pdf").toAbsolutePath().normalize();
        Path destinoTarget = Path.of("target", "informe-muestra-monitoriza.pdf").toAbsolutePath().normalize();
        Files.createDirectories(destinoTarget.getParent());

        Files.write(destinoRaiz, pdf);
        Files.write(destinoTarget, pdf);

        assertTrue(Files.exists(destinoRaiz));
        assertTrue(destinoRaiz.toFile().length() > 500);
        System.out.println("PDF muestra generado en: " + destinoRaiz);
    }

    @Test
    void generarPdfProduceDocumentoValido() {
        ResumenDiaDto resumen = resumenEjemploCompleto();
        GeminiInformeResponseDto analisis = analisisEjemploCompleto();

        byte[] pdf = informePdfService.generarPdf(resumen, analisis);

        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
    }

    @Test
    void formatearFechaTituloCapitaliza() {
        ResumenDiaDto resumen = new ResumenDiaDto();
        resumen.setFecha(LocalDate.of(2026, 6, 12));

        String titulo = informePdfService.formatearFechaTitulo(resumen);

        assertNotNull(titulo);
        assertTrue(Character.isUpperCase(titulo.charAt(0)));
        assertTrue(titulo.contains("2026") || titulo.contains("junio"));
    }

    private ResumenDiaDto resumenEjemploCompleto() {
        ResumenDiaDto resumen = new ResumenDiaDto();
        resumen.setFecha(LocalDate.of(2026, 6, 12));
        resumen.setTotalLecturasClase(48);
        resumen.setTotalLecturasJornada(72);

        SalaInformeDto sala = new SalaInformeDto();
        sala.setNombre("Sala 101");
        sala.setM2(50.0);
        sala.setCantidadEstudiantes(30);
        sala.setCantidadVentanas(2);
        sala.setAireAcondicionado("No");
        sala.setTipoDeVentilacion("cruzada");
        sala.setDescripcionVentilacion("Ventilación cruzada");
        sala.setNumeroPiso(1);
        resumen.setSala(sala);

        List<BloqueMedicionDto> bloques = new ArrayList<>();
        bloques.add(bloque("Antes de clase", "07:30-07:59", false, "22.0", "42.0", "32.0", "120", "430", "150", false, false, false, true, false, false));
        bloques.add(bloque("Primera clase", "08:00-09:29", true, "27.5", "40.0", "38.0", "420", "720", "210", true, false, false, false, true, false));
        bloques.add(bloque("Recreo 1", "09:30-09:49", false, "24.5", "41.0", "45.0", "380", "480", "170", false, false, false, false, false, false));
        bloques.add(bloque("Segunda clase", "09:50-11:19", true, "25.8", "39.0", "36.0", "410", "610", "190", false, false, false, false, false, false));
        bloques.add(bloque("Tercera clase", "11:30-12:59", true, "26.2", "44.0", "52.0", "390", "680", "240", false, false, true, false, false, false));
        bloques.add(bloque("Almuerzo", "13:00-13:59", false, "24.0", "43.0", "33.0", "200", "450", "160", false, false, false, true, false, false));
        bloques.add(bloque("Cuarta clase", "14:00-15:29", true, "25.1", "38.0", "34.0", "450", "590", "175", false, false, false, false, false, false));
        bloques.add(bloque("Después de clase", "15:30-16:30", false, "23.5", "40.0", "31.0", "180", "420", "140", false, false, false, true, false, false));

        resumen.setBloques(bloques);
        return resumen;
    }

    private BloqueMedicionDto bloque(
            String titulo, String horario, boolean clase,
            String temp, String hum, String db, String lux, String co2, String tvoc,
            boolean tAlt, boolean hAlt, boolean dbAlt, boolean lxAlt, boolean coAlt, boolean tvAlt) {
        BloqueMedicionDto b = new BloqueMedicionDto();
        b.setNombre(horario);
        b.setTituloAmigable(titulo);
        b.setHorario(horario);
        b.setBloqueDeClase(clase);
        b.setTemperatura(temp);
        b.setHumedad(hum);
        b.setDb(db);
        b.setLux(lux);
        b.setEco2(co2);
        b.setTvoc(tvoc);
        b.setTemperaturaAlterada(tAlt);
        b.setHumedadAlterada(hAlt);
        b.setDbAlterada(dbAlt);
        b.setLuxAlterada(lxAlt);
        b.setEco2Alterada(coAlt);
        b.setTvocAlterada(tvAlt);

        List<LecturaInformeDto> lecturas = lecturasCada5Minutos(
                horario, temp, hum, db, lux, co2, tvoc, tAlt, hAlt, dbAlt, lxAlt, coAlt, tvAlt);
        b.setCantidadLecturas(lecturas.size());
        b.setLecturas(lecturas);
        return b;
    }

    /** Simula el intervalo del simulador: una lectura cada 5 minutos dentro del bloque. */
    private List<LecturaInformeDto> lecturasCada5Minutos(
            String horario,
            String temp, String hum, String db, String lux, String co2, String tvoc,
            boolean tAlt, boolean hAlt, boolean dbAlt, boolean lxAlt, boolean coAlt, boolean tvAlt) {
        String[] partes = horario.split("-");
        LocalTime inicio = LocalTime.parse(partes[0].trim());
        LocalTime fin = LocalTime.parse(partes[1].trim());
        List<LecturaInformeDto> lecturas = new ArrayList<>();
        LocalTime hora = inicio;
        while (!hora.isAfter(fin)) {
            lecturas.add(lectura(
                    String.format("%02d:%02d", hora.getHour(), hora.getMinute()),
                    temp, hum, db, lux, co2, tvoc,
                    tAlt, hAlt, dbAlt, lxAlt, coAlt, tvAlt));
            hora = hora.plusMinutes(5);
        }
        return lecturas;
    }

    private LecturaInformeDto lectura(
            String hora, String temp, String hum, String db, String lux, String co2, String tvoc,
            boolean tAlt, boolean hAlt, boolean dbAlt, boolean lxAlt, boolean coAlt, boolean tvAlt) {
        LecturaInformeDto l = new LecturaInformeDto();
        l.setHora(hora);
        l.setTemperatura(temp);
        l.setHumedad(hum);
        l.setDb(db);
        l.setLux(lux);
        l.setEco2(co2);
        l.setTvoc(tvoc);
        l.setTemperaturaAlterada(tAlt);
        l.setHumedadAlterada(hAlt);
        l.setDbAlterada(dbAlt);
        l.setLuxAlterada(lxAlt);
        l.setEco2Alterada(coAlt);
        l.setTvocAlterada(tvAlt);
        return l;
    }

    private GeminiInformeResponseDto analisisEjemploCompleto() {
        AlteracionSensorDto temp = new AlteracionSensorDto();
        temp.setSensor("Temperatura");
        temp.setVecesAlterada(8);
        temp.setConsecuencia(
                "Considerando que la sala cuenta con 30 estudiantes en 50 m² (0,6 estudiantes/m²), "
                + "las 8 lecturas con temperatura fuera de rango elevan la carga térmica percibida. "
                + "Esto puede incrementar la fatiga, reducir la atencion sostenida y afectar el rendimiento "
                + "academico, especialmente al mediodia cuando no hay aire acondicionado.");

        AlteracionSensorDto co2 = new AlteracionSensorDto();
        co2.setSensor("Dióxido de Carbono");
        co2.setVecesAlterada(5);
        co2.setConsecuencia(
                "Con ventilacion cruzada y solo 2 ventanas para 30 estudiantes, 5 lecturas elevadas de CO2 "
                + "indican renovacion insuficiente del aire. Esto se asocia con somnolencia, menor velocidad "
                + "de procesamiento y mayor riesgo de dispersion de agentes respiratorios.");

        GeminiInformeResponseDto analisis = new GeminiInformeResponseDto();
        analisis.setContextoSala(
                "La sala 101 tiene 50 m² con 30 estudiantes (0,6 est/m²), 2 ventanas con ventilacion cruzada, "
                + "sin aire acondicionado y ubicada en piso 1. Esta densidad exige mayor atencion a ventilacion y confort termico.");
        analisis.setSeccionAnalisis(List.of(temp, co2));

        SolucionDetalleDto s1 = new SolucionDetalleDto();
        s1.setTitulo("Abrir ventanas");
        s1.setIndicadores(List.of("Dióxido de Carbono", "Temperatura"));
        s1.setExplicacion(
                "Abrir ambas ventanas en recreos y antes de iniciar clases favorece la renovacion del aire. "
                + "En una sala de 50 m² con 30 estudiantes, esto ayuda a reducir CO2 acumulado y temperatura.");

        SolucionDetalleDto s2 = new SolucionDetalleDto();
        s2.setTitulo("Reducir ruido interno");
        s2.setIndicadores(List.of("Decibeles"));
        s2.setExplicacion(
                "Si el ruido proviene del interior, reorganizar grupos y establecer senales visuales de silencio. "
                + "Si es exterior, cerrar ventanas parcialmente en momentos de mayor trafico.");

        SolucionDetalleDto l1 = new SolucionDetalleDto();
        l1.setTitulo("Instalar aire acondicionado");
        l1.setIndicadores(List.of("Temperatura", "Humedad"));
        l1.setExplicacion(
                "Permitiria mantener 20-22 °C de forma estable en verano, mejorando confort y rendimiento cognitivo "
                + "en una sala con alta densidad de estudiantes.");

        SolucionDetalleDto l2 = new SolucionDetalleDto();
        l2.setTitulo("Ventilacion mecanica");
        l2.setIndicadores(List.of("Dióxido de Carbono", "TVOC"));
        l2.setExplicacion(
                "Un sistema de extraccion/inyeccion compensaria la limitacion de solo 2 ventanas para 30 estudiantes.");

        analisis.setSolucionesCortoPlazo(List.of(s1, s2));
        analisis.setSolucionesLargoPlazo(List.of(l1, l2));
        return analisis;
    }

    private ResumenDiaDto resumenEjemplo() {
        ResumenDiaDto resumen = new ResumenDiaDto();
        resumen.setFecha(LocalDate.of(2026, 6, 12));
        resumen.setTotalLecturasClase(12);

        BloqueMedicionDto bloque = new BloqueMedicionDto();
        bloque.setNombre("08:00-09:29");
        bloque.setBloqueDeClase(true);
        bloque.setCantidadLecturas(3);
        bloque.setTemperatura("27.5");
        bloque.setHumedad("40.0");
        bloque.setDb("35.0");
        bloque.setLux("420");
        bloque.setEco2("650");
        bloque.setTvoc("180");
        bloque.setTemperaturaAlterada(true);
        bloque.setHumedadAlterada(false);
        bloque.setDbAlterada(false);
        bloque.setLuxAlterada(false);
        bloque.setEco2Alterada(false);
        bloque.setTvocAlterada(false);

        resumen.setBloques(List.of(bloque));
        return resumen;
    }

    private GeminiInformeResponseDto analisisEjemplo() {
        AlteracionSensorDto alt = new AlteracionSensorDto();
        alt.setSensor("Temperatura");
        alt.setVecesAlterada(1);
        alt.setConsecuencia("Puede reducir la concentracion y aumentar la fatiga en estudiantes.");

        GeminiInformeResponseDto analisis = new GeminiInformeResponseDto();
        analisis.setSeccionAnalisis(List.of(alt));
        analisis.setSolucionCortoPlazo("Abrir ventanas y usar ventiladores durante el recreo.");
        analisis.setSolucionLargoPlazo("Evaluar instalacion de aire acondicionado o climatizacion.");
        return analisis;
    }
}
