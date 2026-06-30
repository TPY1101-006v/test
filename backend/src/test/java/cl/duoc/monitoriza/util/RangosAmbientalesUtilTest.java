package cl.duoc.monitoriza.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RangosAmbientalesUtilTest {

    @Test
    void temperaturaDentroDelRangoFront() {
        assertFalse(RangosAmbientalesUtil.temperaturaAlterada(20.0));
        assertFalse(RangosAmbientalesUtil.temperaturaAlterada(21.0));
        assertFalse(RangosAmbientalesUtil.temperaturaAlterada(22.0));
    }

    @Test
    void temperaturaFueraDelRangoFront() {
        assertTrue(RangosAmbientalesUtil.temperaturaAlterada(19.9));
        assertTrue(RangosAmbientalesUtil.temperaturaAlterada(22.1));
    }

    @Test
    void humedadCoincideConFront() {
        assertFalse(RangosAmbientalesUtil.humedadAlterada(40.0));
        assertFalse(RangosAmbientalesUtil.humedadAlterada(50.0));
        assertFalse(RangosAmbientalesUtil.humedadAlterada(60.0));
        assertTrue(RangosAmbientalesUtil.humedadAlterada(39.9));
        assertTrue(RangosAmbientalesUtil.humedadAlterada(60.1));
    }

    @Test
    void dbCoincideConFront() {
        assertFalse(RangosAmbientalesUtil.dbAlterado(28.0));
        assertFalse(RangosAmbientalesUtil.dbAlterado(35.0));
        assertFalse(RangosAmbientalesUtil.dbAlterado(45.0));
        assertTrue(RangosAmbientalesUtil.dbAlterado(45.1));
    }

    @Test
    void luxCoincideConFront() {
        assertFalse(RangosAmbientalesUtil.luxAlterada(300.0));
        assertFalse(RangosAmbientalesUtil.luxAlterada(400.0));
        assertFalse(RangosAmbientalesUtil.luxAlterada(500.0));
        assertTrue(RangosAmbientalesUtil.luxAlterada(299.0));
        assertTrue(RangosAmbientalesUtil.luxAlterada(501.0));
    }

    @Test
    void eco2CoincideConFront() {
        assertFalse(RangosAmbientalesUtil.eco2Alterado(380.0));
        assertFalse(RangosAmbientalesUtil.eco2Alterado(400.0));
        assertFalse(RangosAmbientalesUtil.eco2Alterado(800.0));
        assertTrue(RangosAmbientalesUtil.eco2Alterado(801.0));
    }

    @Test
    void tvocCoincideConFront() {
        assertFalse(RangosAmbientalesUtil.tvocAlterado(0.0));
        assertFalse(RangosAmbientalesUtil.tvocAlterado(110.0));
        assertFalse(RangosAmbientalesUtil.tvocAlterado(220.0));
        assertTrue(RangosAmbientalesUtil.tvocAlterado(220.1));
    }

    @Test
    void valorNuloNoEsAlterado() {
        assertFalse(RangosAmbientalesUtil.estaAlterado(RangosAmbientalesUtil.Sensor.TEMPERATURA, null));
    }
}
