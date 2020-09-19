package sic.service.impl;

import com.mercadopago.resources.Payment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaReciboCriteria;
import sic.repository.ReciboRepository;
import sic.service.IConfiguracionSucursalService;
import sic.service.IFormaDePagoService;
import sic.service.ISucursalService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ReciboServiceImpl.class})
class ReciboServiceImplTest {

  @MockBean IConfiguracionSucursalService configuracionSucursalServiceInterface;
  @MockBean IFormaDePagoService formaDePagoService;
  @MockBean ISucursalService sucursalService;
  @MockBean ReciboRepository reciboRepository;

  @Autowired ReciboServiceImpl reciboServiceImpl;

  @Test
  void shouldCrearDosRecibos() {
    Sucursal sucursal = new Sucursal();
    sucursal.setNombre("Sucursal Test");
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    configuracionSucursal.setNroPuntoDeVentaAfip(2);
//    when(configuracionSucursalServiceInterface.getConfiguracionSucursal(sucursal))
//        .thenReturn(configuracionSucursal);
    sucursal.setConfiguracionSucursal(configuracionSucursal);
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    FormaDePago formaDePago = new FormaDePago();
    formaDePago.setNombre("Efectivo");
    when(formaDePagoService.getFormasDePagoNoEliminadoPorId(1L)).thenReturn(formaDePago);
    formaDePago.setNombre("Tarjeta Nativa");
    when(formaDePagoService.getFormasDePagoNoEliminadoPorId(2L)).thenReturn(formaDePago);
    when(reciboRepository.findTopBySucursalAndNumSerieOrderByNumReciboDesc(sucursal, 2))
        .thenReturn(null);
    Long[] idsFormasDePago = {1L, 2L};
    BigDecimal[] montos = {new BigDecimal("100"), new BigDecimal("250")};
    Cliente cliente = new Cliente();
    Usuario usuario = new Usuario();
    List<Recibo> recibos =
        reciboServiceImpl.construirRecibos(
            idsFormasDePago, 1L, cliente, usuario, montos, LocalDateTime.now());
    assertEquals(2, recibos.size());
    assertEquals(new BigDecimal("100"), recibos.get(0).getMonto());
    assertEquals(new BigDecimal("250"), recibos.get(1).getMonto());
  }

  @Test
  void shouldCrearUnRecibosConIdsFormasDePagoRepetidos() {
    Sucursal sucursal = new Sucursal();
    sucursal.setNombre("Sucursal Test");
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    configuracionSucursal.setNroPuntoDeVentaAfip(2);
//    when(configuracionSucursalServiceInterface.getConfiguracionSucursal(sucursal))
//        .thenReturn(configuracionSucursal);
    sucursal.setConfiguracionSucursal(configuracionSucursal);
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    FormaDePago formaDePago = new FormaDePago();
    formaDePago.setNombre("Efectivo");
    when(formaDePagoService.getFormasDePagoNoEliminadoPorId(1L)).thenReturn(formaDePago);
    formaDePago.setNombre("Tarjeta Nativa");
    when(formaDePagoService.getFormasDePagoNoEliminadoPorId(2L)).thenReturn(formaDePago);
    when(reciboRepository.findTopBySucursalAndNumSerieOrderByNumReciboDesc(sucursal, 2))
        .thenReturn(null);
    Long[] idsFormasDePago = {2L, 2L};
    BigDecimal[] montos = {new BigDecimal("100"), new BigDecimal("250")};
    Cliente cliente = new Cliente();
    Usuario usuario = new Usuario();
    List<Recibo> recibos =
        reciboServiceImpl.construirRecibos(
            idsFormasDePago, 1L, cliente, usuario, montos, LocalDateTime.now());
    assertEquals(1, recibos.size());
    assertEquals(new BigDecimal("350"), recibos.get(0).getMonto());
  }

  @Test
  void shouldTestBusquedaDeRecibos() {
    BusquedaReciboCriteria busquedaReciboCriteria =
        BusquedaReciboCriteria.builder()
            .fechaDesde(LocalDateTime.MIN)
            .fechaHasta(LocalDateTime.MIN)
            .numSerie(2L)
            .numRecibo(3L)
            .concepto("Recibo por deposito")
            .idCliente(4L)
            .idProveedor(4L)
            .idUsuario(5L)
            .idViajante(6L)
            .movimiento(Movimiento.VENTA)
            .idSucursal(7L)
            .build();
    String builder =
        "containsIc(recibo.concepto,Recibo) && containsIc(recibo.concepto,por) "
            + "&& containsIc(recibo.concepto,deposito) && recibo.numSerie = 2 "
            + "&& recibo.numRecibo = 3 && recibo.cliente.idCliente = 4 "
            + "&& recibo.proveedor.idProveedor = 4 && recibo.usuario.idUsuario = 5 "
            + "&& recibo.cliente.viajante.idUsuario = 6 && recibo.proveedor is null "
            + "&& recibo.sucursal.idSucursal = 7 && recibo.eliminado = false "
            + "&& recibo.fecha between -999999999-01-01T00:00 and -999999999-01-01T23:59:59.999999999";
    assertEquals(builder, reciboServiceImpl.getBuilder(busquedaReciboCriteria).toString());
    builder =
        "containsIc(recibo.concepto,Recibo) && containsIc(recibo.concepto,por) && containsIc(recibo.concepto,deposito) "
            + "&& recibo.numSerie = 2 && recibo.numRecibo = 3 && recibo.cliente.idCliente = 4 "
            + "&& recibo.proveedor.idProveedor = 4 && recibo.usuario.idUsuario = 5 "
            + "&& recibo.cliente.viajante.idUsuario = 6 && recibo.cliente is null "
            + "&& recibo.sucursal.idSucursal = 7 && recibo.eliminado = false && recibo.fecha > -999999999-01-01T00:00";
    busquedaReciboCriteria =
        BusquedaReciboCriteria.builder()
            .fechaDesde(LocalDateTime.MIN)
            .numSerie(2L)
            .numRecibo(3L)
            .concepto("Recibo por deposito")
            .idCliente(4L)
            .idProveedor(4L)
            .idUsuario(5L)
            .idViajante(6L)
            .movimiento(Movimiento.COMPRA)
            .idSucursal(7L)
            .build();
    assertEquals(builder, reciboServiceImpl.getBuilder(busquedaReciboCriteria).toString());
    builder =
        "containsIc(recibo.concepto,Recibo) && containsIc(recibo.concepto,por) "
            + "&& containsIc(recibo.concepto,deposito) && recibo.numSerie = 2 "
            + "&& recibo.numRecibo = 3 && recibo.cliente.idCliente = 4 "
            + "&& recibo.proveedor.idProveedor = 4 && recibo.usuario.idUsuario = 5 "
            + "&& recibo.cliente.viajante.idUsuario = 6 && recibo.formaDePago.idFormaDePago = 3 "
            + "&& recibo.cliente is null && recibo.sucursal.idSucursal = 7 && recibo.eliminado = false "
            + "&& recibo.fecha < -999999999-01-01T23:59:59.999999999";
    busquedaReciboCriteria =
        BusquedaReciboCriteria.builder()
            .fechaHasta(LocalDateTime.MIN)
            .numSerie(2L)
            .numRecibo(3L)
            .concepto("Recibo por deposito")
            .idCliente(4L)
            .idProveedor(4L)
            .idUsuario(5L)
            .idViajante(6L)
            .movimiento(Movimiento.COMPRA)
            .idSucursal(7L)
            .idFormaDePago(3L)
            .build();
    assertEquals(builder, reciboServiceImpl.getBuilder(busquedaReciboCriteria).toString());
  }

  @Test
  void shouldConstruirReciboPorPayment() {
    Sucursal sucursal = new Sucursal();
    Usuario usuario = new Usuario();
    Cliente cliente = new Cliente();
    Payment payment = new Payment();
    payment.setPaymentMethodId("PaymentMethodId");
    payment.setTransactionAmount(100F);
    FormaDePago formaDePago = new FormaDePago();
    formaDePago.setNombre("Mercado Pago");
    when(formaDePagoService.getFormaDePagoPorNombre(FormaDePagoEnum.MERCADO_PAGO))
        .thenReturn(formaDePago);
    Recibo recibo =
        reciboServiceImpl.construirReciboPorPayment(sucursal, usuario, cliente, payment);
    assertNotNull(recibo);
    assertTrue(recibo.getConcepto().startsWith("Pago en MercadoPago"));
    assertEquals(new BigDecimal("100.0"), recibo.getMonto());
  }
}
