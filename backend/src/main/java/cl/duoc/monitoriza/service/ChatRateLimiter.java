package cl.duoc.monitoriza.service;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

@Component
public class ChatRateLimiter {

    private LocalDate diaContador = LocalDate.now();
    private int mensajesHoy;
    private long ventanaMinuto;
    private int mensajesMinuto;

    public synchronized void verificarLimites(int maxPorDia, int maxPorMinuto) {
        LocalDate hoy = LocalDate.now();
        if (!hoy.equals(diaContador)) {
            diaContador = hoy;
            mensajesHoy = 0;
        }

        long minutoActual = System.currentTimeMillis() / 60_000L;
        if (minutoActual != ventanaMinuto) {
            ventanaMinuto = minutoActual;
            mensajesMinuto = 0;
        }

        if (mensajesHoy >= maxPorDia) {
            throw new IllegalStateException(
                    "Límite diario de chat alcanzado (" + maxPorDia + " mensajes). Intenta mañana.");
        }
        if (mensajesMinuto >= maxPorMinuto) {
            throw new IllegalStateException(
                    "Demasiados mensajes seguidos. Espera un minuto e intenta de nuevo.");
        }

        mensajesHoy++;
        mensajesMinuto++;
    }

    synchronized void reiniciar() {
        diaContador = LocalDate.now();
        mensajesHoy = 0;
        ventanaMinuto = 0;
        mensajesMinuto = 0;
    }
}
