package cl.duoc.monitoriza.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import cl.duoc.monitoriza.dto.GeminiInformeResponseDto;
import cl.duoc.monitoriza.dto.RangoIdealInformeDto;
import cl.duoc.monitoriza.dto.ResumenDiaDto;
import cl.duoc.monitoriza.util.RangosAmbientalesUtil;

@Service
public class InformePdfService {

    private static final DateTimeFormatter FECHA_TITULO = DateTimeFormatter
            .ofPattern("EEEE d 'de' MMMM yyyy", new Locale("es", "CL"));

    private final SpringTemplateEngine templateEngine;

    public InformePdfService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generarPdf(ResumenDiaDto resumen, GeminiInformeResponseDto analisis) {
        Context context = new Context(Locale.forLanguageTag("es-CL"));
        context.setVariable("resumen", resumen);
        context.setVariable("analisis", analisis);
        context.setVariable("fechaTitulo", formatearFechaTitulo(resumen));
        context.setVariable("rangosFilas", construirRangosEnFilas());

        String html = templateEngine.process("informe-pdf", context);
        return convertirHtmlAPdf(html);
    }

    List<List<RangoIdealInformeDto>> construirRangosEnFilas() {
        List<RangoIdealInformeDto> todos = construirRangosIdeales();
        return List.of(todos.subList(0, 3), todos.subList(3, 6));
    }

    List<RangoIdealInformeDto> construirRangosIdeales() {
        return List.of(
                new RangoIdealInformeDto("Temperatura", fmtRango(RangosAmbientalesUtil.TEMP_MIN, RangosAmbientalesUtil.TEMP_MAX), "°C"),
                new RangoIdealInformeDto("Humedad", fmtRango(RangosAmbientalesUtil.HUMEDAD_MIN, RangosAmbientalesUtil.HUMEDAD_MAX), "%"),
                new RangoIdealInformeDto("Decibeles", fmtRango(RangosAmbientalesUtil.DB_MIN, RangosAmbientalesUtil.DB_MAX), "dB"),
                new RangoIdealInformeDto("Iluminación", fmtRango(RangosAmbientalesUtil.LUX_MIN, RangosAmbientalesUtil.LUX_MAX), "lx"),
                new RangoIdealInformeDto("Dióxido de Carbono", fmtRango(RangosAmbientalesUtil.ECO2_MIN, RangosAmbientalesUtil.ECO2_MAX), "ppm"),
                new RangoIdealInformeDto("TVOC", fmtRango(RangosAmbientalesUtil.TVOC_MIN, RangosAmbientalesUtil.TVOC_MAX), "ppb")
        );
    }

    private String fmtRango(double min, double max) {
        if (min == (long) min && max == (long) max) {
            return String.format(Locale.US, "%.0f – %.0f", min, max);
        }
        return String.format(Locale.US, "%.0f – %.0f", min, max);
    }

    String formatearFechaTitulo(ResumenDiaDto resumen) {
        if (resumen == null || resumen.getFecha() == null) {
            return "";
        }
        String raw = FECHA_TITULO.format(resumen.getFecha());
        if (raw.isEmpty()) {
            return raw;
        }
        return raw.substring(0, 1).toUpperCase(Locale.forLanguageTag("es-CL")) + raw.substring(1);
    }

    byte[] convertirHtmlAPdf(String html) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el PDF del informe", e);
        }
    }
}
