package sic.service.impl;

import java.util.ResourceBundle;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.springframework.test.context.junit4.SpringRunner;
import sic.builder.ProvinciaBuilder;
import sic.modelo.Localidad;
import sic.modelo.Provincia;
import sic.service.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.repository.LocalidadRepository;

@RunWith(SpringRunner.class)
public class LocalidadServiceImplTest {

    @InjectMocks
    private LocalidadServiceImpl localidadService;   
    
    @Mock
    private LocalidadRepository localidadRepository;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldValidarOperacionWhenNombreVacio() {
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_localidad_vacio_nombre"));
        Localidad localidad = new Localidad();
        localidad.setNombre("");
        localidadService = new LocalidadServiceImpl(localidadRepository);
        localidadService.validarOperacion(TipoDeOperacion.ALTA, localidad);
    }

    @Test
    public void shouldValidarOperacionWhenProvinciaNull() {
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_localidad_provincia_vacio"));
        Localidad localidad = new Localidad();
        localidad.setNombre("Capital");
        localidadService = new LocalidadServiceImpl(localidadRepository);
        localidadService.validarOperacion(TipoDeOperacion.ALTA, localidad);
    }

    @Test
    public void shouldValidarOperacionWhenNombreDuplicadoAlta() {
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_localidad_duplicado_nombre"));
        Provincia provincia = new ProvinciaBuilder().build();
        Localidad localidad = new Localidad();
        localidad.setNombre("Capital");
        localidad.setProvincia(provincia);
        when(localidadRepository.findByNombreAndProvinciaAndEliminadaOrderByNombreAsc("Capital", provincia, false)).thenReturn(localidad);
        localidadService = new LocalidadServiceImpl(localidadRepository);
        localidadService.validarOperacion(TipoDeOperacion.ALTA, localidad);
    }

    @Test
    public void shouldValidarOperacionWhenNombreDuplicadoActualizacion() {
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_localidad_duplicado_nombre"));
        Provincia provincia = new ProvinciaBuilder().build();
        Localidad localidad = new Localidad();
        localidad.setNombre("Capital");
        localidad.setProvincia(provincia);
        localidad.setId_Localidad(Long.MIN_VALUE);
        when(localidadRepository.findByNombreAndProvinciaAndEliminadaOrderByNombreAsc("Capital", provincia, false)).thenReturn(localidad);
        Localidad localidadDuplicada = new Localidad();
        localidadDuplicada.setNombre("Capital");
        localidadDuplicada.setProvincia(provincia);
        localidadDuplicada.setId_Localidad(Long.MAX_VALUE);
        localidadService = new LocalidadServiceImpl(localidadRepository);
        localidadService.validarOperacion(TipoDeOperacion.ACTUALIZACION, localidadDuplicada);
    }

}
