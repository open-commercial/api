package sic.service.impl;

import java.util.ResourceBundle;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.springframework.test.context.junit4.SpringRunner;
import sic.builder.MedidaBuilder;
import sic.modelo.Medida;
import sic.service.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.repository.MedidaRepository;

@RunWith(SpringRunner.class)
public class MedidaServiceImplTest {

    @Mock
    private MedidaRepository medidaRepository;
    
    @InjectMocks
    private MedidaServiceImpl medidaService;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldValidarOperacionWhenNombreVacio() {
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_medida_vacio_nombre"));
        Medida medidaParaTest = new MedidaBuilder().build();
        Medida medida = new MedidaBuilder().build();
        when(medidaRepository.findByNombreAndEmpresaAndEliminada("", medida.getEmpresa(), false)).thenReturn(medidaParaTest);
        medida.setNombre("");
        medidaService.validarOperacion(TipoDeOperacion.ALTA, medida);
    }

    @Test
    public void shouldValidarOperacionWhenNombreDuplicadoAlta() {
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_medida_duplicada_nombre"));
        Medida medidaParaTest = new MedidaBuilder().build();
        Medida medida = new MedidaBuilder().build();
        when(medidaRepository.findByNombreAndEmpresaAndEliminada("Unidad", medida.getEmpresa(), false)).thenReturn(medidaParaTest);
        medida.setNombre("Unidad");        
        medidaService.validarOperacion(TipoDeOperacion.ALTA, medida);
    }

    @Test
    public void shouldValidarOperacionWhenNombreDuplicadoActualizacion() {
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_medida_duplicada_nombre"));
        Medida medidaParaTest = new MedidaBuilder().build();
        Medida medida = new MedidaBuilder().build();
        when(medidaRepository.findByNombreAndEmpresaAndEliminada("Metro", medida.getEmpresa(), false)).thenReturn(medidaParaTest);
        medida.setId_Medida(1L);
        medida.setNombre("Metro");                
        when(medidaService.getMedidaPorNombre("Metro", medida.getEmpresa())).thenReturn(medida);
        Medida medidaDuplicada = new Medida();
        medidaDuplicada.setId_Medida(2L);
        medidaDuplicada.setNombre("Metro");
        medidaDuplicada.setEmpresa(medida.getEmpresa());        
        medidaService.validarOperacion(TipoDeOperacion.ACTUALIZACION, medidaDuplicada);
    }

}
