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
import sic.builder.CondicionIVABuilder;
import sic.modelo.CondicionIVA;
import sic.service.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.repository.CondicionIVARepository;

@RunWith(SpringRunner.class)
public class CondicionIVAServiceImplTest {

    @Mock
    private CondicionIVARepository condicionIVARepository;
    
    @InjectMocks
    private CondicionDeIVAServiceImpl condicionDeIVAServiceImpl;      
   
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldValidarOperacionWhenDuplicadoAlta() {
        CondicionIVA condicionIVA = new CondicionIVABuilder().build();
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_condicionIVA_nombre_duplicado"));
        when(condicionDeIVAServiceImpl.getCondicionIVAPorNombre(condicionIVA.getNombre())).thenReturn(condicionIVA);
        condicionDeIVAServiceImpl.validarOperacion(TipoDeOperacion.ALTA, condicionIVA);
    }

    @Test
    public void shouldValidarOperacionWhenDuplicadoActualizacion() {
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_condicionIVA_nombre_duplicado"));
        CondicionIVA condicionIVA = new CondicionIVABuilder()
                .withId_CondicionIVA(Long.MIN_VALUE)
                .withNombre("Responsable Inscripto")
                .build();        
        CondicionIVA condicionIVADuplicada = new CondicionIVABuilder().build();
        condicionIVADuplicada.setId_CondicionIVA(Long.MAX_VALUE);
        condicionIVADuplicada.setNombre("Responsable Inscripto");        
        when(condicionDeIVAServiceImpl.getCondicionIVAPorNombre(condicionIVADuplicada.getNombre())).thenReturn(condicionIVA);
        condicionDeIVAServiceImpl.validarOperacion(TipoDeOperacion.ACTUALIZACION, condicionIVADuplicada);
    }

}
