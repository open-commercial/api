package sic.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.springframework.test.context.junit4.SpringRunner;
import sic.builder.ClienteBuilder;
import sic.builder.EmpresaBuilder;
import sic.builder.RenglonNotaCreditoBuilder;
import sic.modelo.Cliente;
import sic.modelo.CondicionIVA;
import sic.modelo.Empresa;
import sic.modelo.RenglonNotaCredito;
import sic.modelo.TipoDeComprobante;

@RunWith(SpringRunner.class)
public class NotaServiceImplTest {

    @Mock
    private EmpresaServiceImpl empresaServiceImpl;
    
    @Mock
    private ClienteServiceImpl clienteService;
    
    @InjectMocks
    private NotaServiceImpl notaServiceImpl;
    
    @Test
    public void shouldGetTipoNotaWhenEmpresaYClienteDiscriminanIVA() {
        Empresa empresa = new EmpresaBuilder().build();
        CondicionIVA condicionIVA = new CondicionIVA();
        condicionIVA.setDiscriminaIVA(true);
        Cliente cliente = new Cliente();
        cliente.setCondicionIVA(condicionIVA);
        when(empresaServiceImpl.getEmpresaPorId(1L)).thenReturn(empresa);
        when(clienteService.getClientePorId(1L)).thenReturn(cliente);
        TipoDeComprobante[] expResult = {TipoDeComprobante.NOTA_CREDITO_A,
                                         TipoDeComprobante.NOTA_CREDITO_X,
                                         TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO,
                                         TipoDeComprobante.NOTA_DEBITO_A,
                                         TipoDeComprobante.NOTA_DEBITO_X,
                                         TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO};
        TipoDeComprobante[] result = notaServiceImpl.getTipoNotaCliente(1L, 1L);
        assertArrayEquals(expResult, result);
    }
    
    @Test
    public void shouldGetTipoNotaWhenEmpresaDiscriminaYClienteNoIVA() {
        Empresa empresa = new EmpresaBuilder().build();
        CondicionIVA condicionIVA = new CondicionIVA();
        condicionIVA.setDiscriminaIVA(false);
        Cliente cliente = new Cliente();
        cliente.setCondicionIVA(condicionIVA);
        CondicionIVA condicionIVAqueDiscrimina = Mockito.mock(CondicionIVA.class);
        when(condicionIVAqueDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.FALSE);
        cliente.setCondicionIVA(condicionIVAqueDiscrimina);
        when(empresaServiceImpl.getEmpresaPorId(1L)).thenReturn(empresa);
        when(clienteService.getClientePorId(1L)).thenReturn(cliente);
        TipoDeComprobante[] expResult = {TipoDeComprobante.NOTA_CREDITO_B,
                                         TipoDeComprobante.NOTA_CREDITO_X,
                                         TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO,
                                         TipoDeComprobante.NOTA_DEBITO_B,
                                         TipoDeComprobante.NOTA_DEBITO_X,
                                         TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO};
        TipoDeComprobante[] result = notaServiceImpl.getTipoNotaCliente(1L, 1L);
        assertArrayEquals(expResult, result);
    }
    
    @Test
    public void shouldGetTipoNotaWhenEmpresaNoDiscriminaYClienteSiIVA() {
        Empresa empresa = new EmpresaBuilder().build();
        CondicionIVA condicionIVA = new CondicionIVA();
        condicionIVA.setDiscriminaIVA(true);
        Cliente cliente = new Cliente();
        cliente.setCondicionIVA(condicionIVA);
        CondicionIVA condicionIVAqueDiscrimina = Mockito.mock(CondicionIVA.class);
        when(condicionIVAqueDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.FALSE);
        empresa.setCondicionIVA(condicionIVAqueDiscrimina);
        when(empresaServiceImpl.getEmpresaPorId(1L)).thenReturn(empresa);
        when(clienteService.getClientePorId(1L)).thenReturn(cliente);
        TipoDeComprobante[] expResult = {TipoDeComprobante.NOTA_CREDITO_X,
                                         TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO,
                                         TipoDeComprobante.NOTA_DEBITO_X,
                                         TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO};
        TipoDeComprobante[] result = notaServiceImpl.getTipoNotaCliente(1L, 1L);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void shouldGetTipoNotaWhenEmpresaNoDiscriminaYClienteNoIVA() {
        Empresa empresa = new EmpresaBuilder()
                          .withId_Empresa(1L)
                          .build();
        CondicionIVA condicionIVA = new CondicionIVA();
        condicionIVA.setDiscriminaIVA(false);
        Cliente cliente = new Cliente();
        cliente.setCondicionIVA(condicionIVA);
        CondicionIVA condicionIVAqueDiscrimina = Mockito.mock(CondicionIVA.class);
        when(condicionIVAqueDiscrimina.isDiscriminaIVA()).thenReturn(Boolean.FALSE);
        empresa.setCondicionIVA(condicionIVAqueDiscrimina);
        when(empresaServiceImpl.getEmpresaPorId(1L)).thenReturn(empresa);
        when(clienteService.getClientePorId(1L)).thenReturn(cliente);
        TipoDeComprobante[] expResult = {TipoDeComprobante.NOTA_CREDITO_X,
                                         TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO,
                                         TipoDeComprobante.NOTA_DEBITO_X,
                                         TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO};
        TipoDeComprobante[] result = notaServiceImpl.getTipoNotaCliente(1L, 1L);
        assertArrayEquals(expResult, result);
    }

  @Test
  public void shouldCalcularTotalNotaCredito() {
    RenglonNotaCredito renglon1 = new RenglonNotaCreditoBuilder().build();
    List<RenglonNotaCredito> renglones = new ArrayList<>();
    renglones.add(renglon1);
    assertTrue(
        "El total de la nota de credito no es el esperado",
        (new BigDecimal("172.062")).compareTo(notaServiceImpl.calcularTotalNota(renglones)) == 0);
  }
}
