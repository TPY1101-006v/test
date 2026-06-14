package cl.duoc.monitoriza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cl.duoc.monitoriza.model.Institucion;
import cl.duoc.monitoriza.service.InstitucionService;

class InstitucionControllerTest {

    @Mock
    private InstitucionService institucionService;

    @InjectMocks
    private InstitucionController institucionController;

    private Institucion institucion;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        institucion = new Institucion(1L, "Universidad Test", "Calle Principal 123");
    }

    @Test
    void testListar() {
        // Arrange
        Institucion inst2 = new Institucion(2L, "Instituto Test", "Avenida Secundaria 456");
        List<Institucion> instituciones = Arrays.asList(institucion, inst2);
        when(institucionService.listar()).thenReturn(instituciones);

        // Act
        List<Institucion> result = institucionController.listar();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Universidad Test", result.get(0).getNombre());
        assertEquals("Instituto Test", result.get(1).getNombre());
        verify(institucionService, times(1)).listar();
    }

    @Test
    void testListarEmpty() {
        // Arrange
        when(institucionService.listar()).thenReturn(Arrays.asList());

        // Act
        List<Institucion> result = institucionController.listar();

        // Assert
        assertEquals(0, result.size());
        verify(institucionService, times(1)).listar();
    }

    @Test
    void testCrear() {
        // Arrange
        when(institucionService.crear(institucion)).thenReturn(institucion);

        // Act
        Institucion result = institucionController.crear(institucion);

        // Assert
        assertNotNull(result);
        assertEquals("Universidad Test", result.getNombre());
        assertEquals("Calle Principal 123", result.getDireccion());
        verify(institucionService, times(1)).crear(institucion);
    }

    @Test
    void testCrearNewInstitucion() {
        // Arrange
        Institucion newInstitucion = new Institucion("Nueva Universidad", "Nueva Dirección");
        when(institucionService.crear(newInstitucion)).thenReturn(newInstitucion);

        // Act
        Institucion result = institucionController.crear(newInstitucion);

        // Assert
        assertNotNull(result);
        assertEquals("Nueva Universidad", result.getNombre());
        assertEquals("Nueva Dirección", result.getDireccion());
        verify(institucionService, times(1)).crear(newInstitucion);
    }

    @Test
    void testCrearCallsService() {
        // Arrange
        when(institucionService.crear(institucion)).thenReturn(institucion);

        // Act
        institucionController.crear(institucion);

        // Assert
        verify(institucionService).crear(institucion);
    }
}
