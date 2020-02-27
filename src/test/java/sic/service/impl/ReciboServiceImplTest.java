package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.*;
import sic.repository.ReciboRepository;
import sic.service.IConfiguracionSucursalService;
import sic.service.IFormaDePagoService;
import sic.service.ISucursalService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ReciboServiceImplTest {

  @Mock private IConfiguracionSucursalService configuracionSucursalServiceInterfaceMock;
  @Mock private IFormaDePagoService formaDePagoServiceInterfaceMock;
  @Mock private ISucursalService sucursalServiceInterfaceMock;
  @Mock private ReciboRepository reciboRepositoryMock;

  @InjectMocks private ReciboServiceImpl reciboServiceImpl;

  @Test
  void shouldCrearDosRecibos() {
    Sucursal sucursal = new Sucursal();
    sucursal.setNombre("Sucursal Test");
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    configuracionSucursal.setNroPuntoDeVentaAfip(2);
    when(configuracionSucursalServiceInterfaceMock.getConfiguracionSucursal(sucursal))
        .thenReturn(configuracionSucursal);
    when(sucursalServiceInterfaceMock.getSucursalPorId(1L)).thenReturn(sucursal);
    FormaDePago formaDePago = new FormaDePago();
    formaDePago.setNombre("Efectivo");
    when(formaDePagoServiceInterfaceMock.getFormasDePagoNoEliminadoPorId(1L)).thenReturn(formaDePago);
    formaDePago.setNombre("Tarjeta Nativa");
    when(formaDePagoServiceInterfaceMock.getFormasDePagoNoEliminadoPorId(2L)).thenReturn(formaDePago);
    when(reciboRepositoryMock.findTopBySucursalAndNumSerieOrderByNumReciboDesc(sucursal, 2)).thenReturn(null);
    Long[] idsFormasDePago = {1L, 2L};
    BigDecimal[] montos = {new BigDecimal("100"), new BigDecimal("250")};
    BigDecimal totalFactura = new BigDecimal("350");
    Cliente cliente = new Cliente();
    Usuario usuario = new Usuario();
    List<Recibo> recibos =
        reciboServiceImpl.construirRecibos(
            idsFormasDePago, sucursal, cliente, usuario, montos, totalFactura, LocalDateTime.now());
    assertEquals(2, recibos.size());
    assertEquals(new BigDecimal("100"), recibos.get(0).getMonto());
    assertEquals(new BigDecimal("250"), recibos.get(1).getMonto());
  }
}
