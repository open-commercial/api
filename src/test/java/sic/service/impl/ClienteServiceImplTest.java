package sic.service.impl;

import java.util.ResourceBundle;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.mockito.Mockito.when;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import sic.builder.ClienteBuilder;
import sic.builder.CondicionIVABuilder;
import sic.builder.EmpresaBuilder;
import sic.builder.LocalidadBuilder;
import sic.modelo.Cliente;
import sic.service.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.repository.ClienteRepository;

@RunWith(SpringRunner.class)
public class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;
    
    @InjectMocks
    private ClienteServiceImpl clienteServiceImpl;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldSetClientePredeterminado() {
        Cliente resultadoEsperado = new ClienteBuilder().build();
        clienteServiceImpl.setClientePredeterminado(resultadoEsperado);
        when(clienteRepository.findByAndEmpresaAndPredeterminadoAndEliminado((new EmpresaBuilder()).build(), true, false))
                                     .thenReturn((new ClienteBuilder()).build());
        Cliente resultadoObtenido = clienteServiceImpl.getClientePredeterminado((new EmpresaBuilder()).build());
        assertEquals(resultadoEsperado, resultadoObtenido);
    }
    
    @Test
    public void shouldValidarOperacionWhenEmailInvalido() {
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_email_invalido"));
        clienteServiceImpl.validarOperacion(TipoDeOperacion.ELIMINACION, new ClienteBuilder().withEmail("@@.com").build());        
    }

    @Test
    public void shouldValidarOperacionWhenCondicionIVAesNull() {
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_vacio_condicionIVA"));
        clienteServiceImpl.validarOperacion(TipoDeOperacion.ELIMINACION, new ClienteBuilder()
                .withEmail("soporte@gmail.com")
                .withRazonSocial("Ferreteria Julian")
                .withCondicionIVA(null)
                .build());
    }

    @Test
    public void shouldValidarOperacionWhenLocalidadEsNull() {        
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_vacio_localidad"));
        clienteServiceImpl.validarOperacion(TipoDeOperacion.ELIMINACION, new ClienteBuilder()
                .withEmail("soporte@gmail.com")
                .withRazonSocial("Ferreteria Julian")
                .withLocalidad(null)
                .withCondicionIVA(new CondicionIVABuilder().build())
                .build());
    }

    @Test
    public void shouldValidarOperacionWhenEmpresaEsNull() {
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_vacio_empresa"));
        Cliente cliente = new ClienteBuilder()
                .withEmail("soporte@gmail.com")
                .withRazonSocial("Ferreteria Julian")
                .withCondicionIVA(new CondicionIVABuilder().build())
                .withLocalidad(new LocalidadBuilder().build())
                .withEmpresa(null)
                .build();       
        clienteServiceImpl.validarOperacion(TipoDeOperacion.ELIMINACION, cliente);
    }

    @Test
    public void shouldValidarOperacionWhenIdFiscalDuplicadoEnAlta() {
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_duplicado_idFiscal"));
        Cliente cliente = new ClienteBuilder().build();
        Cliente clienteDuplicado = new ClienteBuilder().build();
        when(clienteRepository.findByIdFiscalAndEmpresaAndEliminado(cliente.getIdFiscal(), cliente.getEmpresa(), false))
                .thenReturn(cliente);        
        clienteServiceImpl.validarOperacion(TipoDeOperacion.ALTA, clienteDuplicado);
    }

    @Test
    public void shouldValidarOperacionWhenIdFiscalDuplicadoEnActualizacion() {
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_duplicado_idFiscal"));
        Cliente cliente = new ClienteBuilder()
                .withId_Cliente(7L)
                .withRazonSocial("Merceria los dos botones")
                .withIdFiscal("23111111119")
                .build();
        Cliente clienteDuplicado = new ClienteBuilder()
                .withId_Cliente(2L)
                .withRazonSocial("Merceria los dos botones")
                .withIdFiscal("23111111119")
                .build();
        when(clienteRepository.findByIdFiscalAndEmpresaAndEliminado(cliente.getIdFiscal(), cliente.getEmpresa(), false))
                .thenReturn(cliente);        
        clienteServiceImpl.validarOperacion(TipoDeOperacion.ACTUALIZACION, clienteDuplicado);
    }

    @Test
    public void shouldValidarOperacionWhenRazonSocialDuplicadaEnAlta() {
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_duplicado_razonSocial"));
        Cliente cliente = new ClienteBuilder()
                .withEmail("soporte@gmail.com")
                .withRazonSocial("Ferreteria Julian")
                .withCondicionIVA(new CondicionIVABuilder().build())
                .withLocalidad(new LocalidadBuilder().build())
                .withEmpresa(new EmpresaBuilder().build())
                .withIdFiscal("23111111119")
                .withId_Cliente(Long.MIN_VALUE)
                .build();
        Cliente clienteDuplicado = new ClienteBuilder()
                .withEmail("soporte@gmail.com")
                .withRazonSocial("Ferreteria Julian")
                .withCondicionIVA(new CondicionIVABuilder().build())
                .withLocalidad(new LocalidadBuilder().build())
                .withEmpresa(new EmpresaBuilder().build())
                .withIdFiscal("23111111119")
                .withId_Cliente(Long.MIN_VALUE)
                .build();
        when(clienteRepository.findByRazonSocialAndEmpresaAndEliminado(cliente.getRazonSocial(), cliente.getEmpresa(), false))
                .thenReturn(cliente);        
        clienteServiceImpl.validarOperacion(TipoDeOperacion.ALTA, clienteDuplicado);
    }

    @Test
    public void shouldValidarOperacionWhenRazonSocialDuplicadaEnActualizacion() {
        thrown.expect(BusinessServiceException.class);
        thrown.expectMessage(ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_duplicado_razonSocial"));
        Cliente cliente = new ClienteBuilder()
                .withId_Cliente(2L)
                .withRazonSocial("Ferreteria Julian")
                .build();
        Cliente clienteDuplicado = new ClienteBuilder()
                .withId_Cliente(4L)
                .withRazonSocial("Ferreteria Julian")
                .build();
        when(clienteRepository.findByRazonSocialAndEmpresaAndEliminado(cliente.getRazonSocial(), cliente.getEmpresa(), false))
                .thenReturn(cliente);        
        clienteServiceImpl.validarOperacion(TipoDeOperacion.ACTUALIZACION, clienteDuplicado);
    }
}
