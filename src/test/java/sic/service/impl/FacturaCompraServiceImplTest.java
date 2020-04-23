package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaFacturaCompraCriteria;
import sic.repository.FacturaCompraRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppTest.class)
class FacturaCompraServiceImplTest {

  @Autowired MessageSource messageSourceTest;

  @Mock FacturaCompraRepository mockFacturaCompraRepository;
  @Mock MessageSource messageSourceTestMock;
  @InjectMocks FacturaServiceImpl facturaServiceImpl;
  @InjectMocks FacturaCompraServiceImpl facturaCompraServiceImpl;

  @Test
  void shouldGetTipoFacturaCompraWhenSucursalYProveedorDiscriminanIVA() {
    Sucursal sucursal = Mockito.mock(Sucursal.class);
    Proveedor proveedor = Mockito.mock(Proveedor.class);
    when(sucursal.getCategoriaIVA()).thenReturn(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    when(proveedor.getCategoriaIVA()).thenReturn(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    TipoDeComprobante[] expResult = new TipoDeComprobante[4];
    expResult[0] = TipoDeComprobante.FACTURA_A;
    expResult[1] = TipoDeComprobante.FACTURA_B;
    expResult[2] = TipoDeComprobante.FACTURA_X;
    expResult[3] = TipoDeComprobante.PRESUPUESTO;
    TipoDeComprobante[] result =
        facturaCompraServiceImpl.getTiposDeComprobanteCompra(sucursal, proveedor);
    assertArrayEquals(expResult, result);
  }

  @Test
  void shouldGetTipoFacturaCompraWhenSucursalDiscriminaIVAYProveedorNO() {
    Sucursal sucursal = Mockito.mock(Sucursal.class);
    Proveedor proveedor = Mockito.mock(Proveedor.class);
    when(sucursal.getCategoriaIVA()).thenReturn(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    when(proveedor.getCategoriaIVA()).thenReturn(CategoriaIVA.MONOTRIBUTO);
    TipoDeComprobante[] expResult = new TipoDeComprobante[3];
    expResult[0] = TipoDeComprobante.FACTURA_C;
    expResult[1] = TipoDeComprobante.FACTURA_X;
    expResult[2] = TipoDeComprobante.PRESUPUESTO;
    TipoDeComprobante[] result =
        facturaCompraServiceImpl.getTiposDeComprobanteCompra(sucursal, proveedor);
    assertArrayEquals(expResult, result);
  }

  @Test
  void shouldGetTipoFacturaCompraWhenSucursalNoDiscriminaIVAYProveedorSI() {
    Sucursal sucursal = Mockito.mock(Sucursal.class);
    Proveedor proveedor = Mockito.mock(Proveedor.class);
    when(sucursal.getCategoriaIVA()).thenReturn(CategoriaIVA.MONOTRIBUTO);
    when(proveedor.getCategoriaIVA()).thenReturn(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    TipoDeComprobante[] expResult = new TipoDeComprobante[3];
    expResult[0] = TipoDeComprobante.FACTURA_B;
    expResult[1] = TipoDeComprobante.FACTURA_X;
    expResult[2] = TipoDeComprobante.PRESUPUESTO;
    TipoDeComprobante[] result =
        facturaCompraServiceImpl.getTiposDeComprobanteCompra(sucursal, proveedor);
    assertArrayEquals(expResult, result);
  }

  @Test
  void shouldGetTipoFacturaCompraWhenSucursalNoDiscriminaYProveedorTampoco() {
    Sucursal sucursal = Mockito.mock(Sucursal.class);
    Proveedor proveedor = Mockito.mock(Proveedor.class);
    when(sucursal.getCategoriaIVA()).thenReturn(CategoriaIVA.MONOTRIBUTO);
    when(proveedor.getCategoriaIVA()).thenReturn(CategoriaIVA.MONOTRIBUTO);
    TipoDeComprobante[] expResult = new TipoDeComprobante[3];
    expResult[0] = TipoDeComprobante.FACTURA_C;
    expResult[1] = TipoDeComprobante.FACTURA_X;
    expResult[2] = TipoDeComprobante.PRESUPUESTO;
    TipoDeComprobante[] result =
        facturaCompraServiceImpl.getTiposDeComprobanteCompra(sucursal, proveedor);
    assertArrayEquals(expResult, result);
  }

  @Test
  void shouldCalcularIVANetoWhenCompraConFacturaA() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        0,
        facturaServiceImpl
            .calcularIVANetoRenglon(
                Movimiento.COMPRA, TipoDeComprobante.FACTURA_A, producto, BigDecimal.ZERO)
            .compareTo(new BigDecimal("21")));
  }

  @Test
  void shouldCalcularIVANetoWhenCompraConFacturaB() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("200"));
    producto.setPrecioVentaPublico(new BigDecimal("1000"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        42,
        facturaServiceImpl
            .calcularIVANetoRenglon(
                Movimiento.COMPRA, TipoDeComprobante.FACTURA_B, producto, BigDecimal.ZERO)
            .doubleValue());
  }

  @Test
  void shouldTestBusquedaFacturaCompraCriteria() {
    BusquedaFacturaCompraCriteria criteria =
        BusquedaFacturaCompraCriteria.builder()
            .idSucursal(1L)
            .fechaDesde(LocalDateTime.MIN)
            .fechaHasta(LocalDateTime.MIN)
            .idProveedor(2L)
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .idProducto(3L)
            .numSerie(4L)
            .numFactura(5L)
            .build();
    String resultadoBuilder =
        "facturaCompra.sucursal.idSucursal = 1 && facturaCompra.eliminada = false && facturaCompra.fecha "
            + "between -999999999-01-01T00:00 and -999999999-01-01T23:59:59.999999999 "
            + "&& facturaCompra.proveedor.idProveedor = 2 "
            + "&& facturaCompra.tipoComprobante = FACTURA_A "
            + "&& any(facturaCompra.renglones).idProductoItem = 3 "
            + "&& facturaCompra.numSerie = 4 && facturaCompra.numFactura = 5";
    assertEquals(resultadoBuilder, facturaCompraServiceImpl.getBuilderCompra(criteria).toString());
    criteria =
        BusquedaFacturaCompraCriteria.builder()
            .idSucursal(1L)
            .fechaDesde(LocalDateTime.MIN)
            .idProveedor(2L)
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .idProducto(3L)
            .numSerie(4L)
            .numFactura(5L)
            .build();
    resultadoBuilder =
        "facturaCompra.sucursal.idSucursal = 1 && facturaCompra.eliminada = false "
            + "&& facturaCompra.fecha > -999999999-01-01T00:00 && facturaCompra.proveedor.idProveedor = 2 "
            + "&& facturaCompra.tipoComprobante = FACTURA_A && any(facturaCompra.renglones).idProductoItem = 3 "
            + "&& facturaCompra.numSerie = 4 && facturaCompra.numFactura = 5";
    assertEquals(resultadoBuilder, facturaCompraServiceImpl.getBuilderCompra(criteria).toString());
    criteria =
        BusquedaFacturaCompraCriteria.builder()
            .idSucursal(1L)
            .fechaHasta(LocalDateTime.MIN)
            .idProveedor(2L)
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .idProducto(3L)
            .numSerie(4L)
            .numFactura(5L)
            .build();
    resultadoBuilder =
        "facturaCompra.sucursal.idSucursal = 1 && facturaCompra.eliminada = false "
            + "&& facturaCompra.fecha < -999999999-01-01T23:59:59.999999999 "
            + "&& facturaCompra.proveedor.idProveedor = 2 && facturaCompra.tipoComprobante = FACTURA_A "
            + "&& any(facturaCompra.renglones).idProductoItem = 3 && facturaCompra.numSerie = 4 "
            + "&& facturaCompra.numFactura = 5";
    assertEquals(resultadoBuilder, facturaCompraServiceImpl.getBuilderCompra(criteria).toString());
  }

  @Test
  void shouldThrownBusinessServiceExceptionPorBusquedaCompraSinIdSucursal() {
    BusquedaFacturaCompraCriteria criteria = BusquedaFacturaCompraCriteria.builder().build();
    when(messageSourceTestMock.getMessage(
            "mensaje_busqueda_sin_sucursal", null, Locale.getDefault()))
        .thenReturn(
            messageSourceTest.getMessage(
                "mensaje_busqueda_sin_sucursal", null, Locale.getDefault()));
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> facturaCompraServiceImpl.getBuilderCompra(criteria));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSourceTest.getMessage(
                    "mensaje_busqueda_sin_sucursal", null, Locale.getDefault())));
  }

  @Test
  void shouldCalcularTotalFacturadoCompra() {
    BusquedaFacturaCompraCriteria criteria =
        BusquedaFacturaCompraCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaCompra qFacturaCompra = QFacturaCompra.facturaCompra;
    builder.and(
        qFacturaCompra
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturaCompra.eliminada.eq(false)));
    when(mockFacturaCompraRepository.calcularTotalFacturadoCompra(builder))
        .thenReturn(BigDecimal.TEN);
    assertEquals(BigDecimal.TEN, facturaCompraServiceImpl.calcularTotalFacturadoCompra(criteria));
  }

  @Test
  void shouldCalcularTotalFacturadoCompraAndReturnZero() {
    BusquedaFacturaCompraCriteria criteria =
        BusquedaFacturaCompraCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaCompra qFacturaCompra = QFacturaCompra.facturaCompra;
    builder.and(
        qFacturaCompra
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturaCompra.eliminada.eq(false)));
    when(mockFacturaCompraRepository.calcularTotalFacturadoCompra(builder)).thenReturn(null);
    assertEquals(BigDecimal.ZERO, facturaCompraServiceImpl.calcularTotalFacturadoCompra(criteria));
  }

  @Test
  void shouldCalcularIvaCompra() {
    BusquedaFacturaCompraCriteria criteria =
        BusquedaFacturaCompraCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaCompra qFacturaCompra = QFacturaCompra.facturaCompra;
    builder.and(
        qFacturaCompra
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturaCompra.eliminada.eq(false)));
    TipoDeComprobante[] tipoFactura = {TipoDeComprobante.FACTURA_A};
    when(mockFacturaCompraRepository.calcularIVACompra(builder, tipoFactura))
        .thenReturn(BigDecimal.TEN);
    assertEquals(BigDecimal.TEN, facturaCompraServiceImpl.calcularIvaCompra(criteria));
  }

  @Test
  void shouldCalcularIvaCompraAndReturnZero() {
    BusquedaFacturaCompraCriteria criteria =
        BusquedaFacturaCompraCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaCompra qFacturaCompra = QFacturaCompra.facturaCompra;
    builder.and(
        qFacturaCompra
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturaCompra.eliminada.eq(false)));
    TipoDeComprobante[] tipoFactura = {TipoDeComprobante.FACTURA_A};
    when(mockFacturaCompraRepository.calcularIVACompra(builder, tipoFactura)).thenReturn(null);
    assertEquals(BigDecimal.ZERO, facturaCompraServiceImpl.calcularIvaCompra(criteria));
  }
}
