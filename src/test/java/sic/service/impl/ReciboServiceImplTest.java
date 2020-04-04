package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ReciboServiceImplTest {

  @Mock IConfiguracionSucursalService configuracionSucursalServiceInterfaceMock;
  @Mock IFormaDePagoService formaDePagoServiceInterfaceMock;
  @Mock ISucursalService sucursalServiceInterfaceMock;
  @Mock ReciboRepository reciboRepositoryMock;

  @InjectMocks ReciboServiceImpl reciboServiceImpl;

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

  @Test
  void shouldCrearUnRecibosConIdsFormasDePagoRepetidos() {
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
    Long[] idsFormasDePago = {2L, 2L};
    BigDecimal[] montos = {new BigDecimal("100"), new BigDecimal("250")};
    BigDecimal totalFactura = new BigDecimal("350");
    Cliente cliente = new Cliente();
    Usuario usuario = new Usuario();
    List<Recibo> recibos =
            reciboServiceImpl.construirRecibos(
                    idsFormasDePago, sucursal, cliente, usuario, montos, totalFactura, LocalDateTime.now());
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
        "(recibo.fecha between -999999999-01-01T00:00 and -999999999-01-01T23:59:59.999999999 "
            + "&& recibo.numSerie = 2 && recibo.numRecibo = 3 || containsIc(recibo.concepto,Recibo) "
            + "&& containsIc(recibo.concepto,por) && containsIc(recibo.concepto,deposito)) "
            + "&& recibo.cliente.idCliente = 4 && recibo.proveedor.idProveedor = 4 && recibo.usuario.idUsuario = 5 "
            + "&& recibo.cliente.viajante.idUsuario = 6 && recibo.proveedor is null && recibo.sucursal.idSucursal = 7 "
            + "&& recibo.eliminado = false";
    assertEquals(builder, reciboServiceImpl.getBuilder(busquedaReciboCriteria).toString());
    builder =
        "(recibo.fecha > -999999999-01-01T00:00 && recibo.numSerie = 2 && recibo.numRecibo = 3 "
            + "|| containsIc(recibo.concepto,Recibo) && containsIc(recibo.concepto,por) "
            + "&& containsIc(recibo.concepto,deposito)) && recibo.cliente.idCliente = 4 "
            + "&& recibo.proveedor.idProveedor = 4 && recibo.usuario.idUsuario = 5 && recibo.cliente.viajante.idUsuario = 6 "
            + "&& recibo.cliente is null && recibo.sucursal.idSucursal = 7 && recibo.eliminado = false";
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
        "(recibo.fecha < -999999999-01-01T23:59:59.999999999 && recibo.numSerie = 2 && recibo.numRecibo = 3 "
            + "|| containsIc(recibo.concepto,Recibo) && containsIc(recibo.concepto,por) "
            + "&& containsIc(recibo.concepto,deposito)) && recibo.cliente.idCliente = 4 "
            + "&& recibo.proveedor.idProveedor = 4 && recibo.usuario.idUsuario = 5 "
            + "&& recibo.cliente.viajante.idUsuario = 6 && recibo.cliente is null "
            + "&& recibo.sucursal.idSucursal = 7 && recibo.eliminado = false";
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
            .build();
    assertEquals(builder, reciboServiceImpl.getBuilder(busquedaReciboCriteria).toString());
  }
}
