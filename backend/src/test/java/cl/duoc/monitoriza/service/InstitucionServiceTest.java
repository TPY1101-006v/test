package cl.duoc.monitoriza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import cl.duoc.monitoriza.repository.InstitucionRepository;

class InstitucionServiceTest {

    @Mock
    private InstitucionRepository institucionRepository;

    @InjectMocks
    private InstitucionService institucionService;

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
        when(institucionRepository.findAll()).thenReturn(instituciones);

        // Act
        List<Institucion> result = institucionService.listar();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Universidad Test", result.get(0).getNombre());
        assertEquals("Instituto Test", result.get(1).getNombre());
        verify(institucionRepository, times(1)).findAll();
    }

    @Test
    void testListarEmpty() {
        // Arrange
        when(institucionRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Institucion> result = institucionService.listar();

        // Assert
        assertEquals(0, result.size());
        verify(institucionRepository, times(1)).findAll();
    }

    @Test
    void testCrear() {
        // Arrange
        when(institucionRepository.save(institucion)).thenReturn(institucion);

        // Act
        Institucion result = institucionService.crear(institucion);

        // Assert
        assertNotNull(result);
        assertEquals("Universidad Test", result.getNombre());
        assertEquals("Calle Principal 123", result.getDireccion());
        verify(institucionRepository, times(1)).save(institucion);
    }

    @Test
    void testCrearWithoutId() {
        // Arrange
        Institucion newInstitucion = new Institucion("Nueva Universidad", "Nueva Dirección");
        when(institucionRepository.save(newInstitucion)).thenReturn(newInstitucion);

        // Act
        Institucion result = institucionService.crear(newInstitucion);

        // Assert
        assertNotNull(result);
        assertEquals("Nueva Universidad", result.getNombre());
        assertEquals("Nueva Dirección", result.getDireccion());
        verify(institucionRepository, times(1)).save(newInstitucion);
    }

    @Test
    void testCrearAndSaveCalled() {
        // Arrange
        when(institucionRepository.save(institucion)).thenReturn(institucion);

        // Act
        Institucion result = institucionService.crear(institucion);

        // Assert
        assertSame(institucion, result);
        verify(institucionRepository).save(institucion);
    }
}
