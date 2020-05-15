package sic.service.impl;

import java.math.BigDecimal;
import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.dto.NuevoRenglonFacturaDTO;
import sic.repository.FacturaRepository;
import sic.util.CalculosComprobante;
import sic.util.CustomValidator;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {CustomValidator.class, FacturaServiceImpl.class, MessageSource.class})
class FacturaServiceImplTest {

  @MockBean SucursalServiceImpl sucursalService;
  @MockBean ProductoServiceImpl productoService;
  @MockBean PedidoServiceImpl pedidoService;
  @MockBean FacturaRepository<Factura> facturaRepository;
  @MockBean NotaServiceImpl notaService;
  @MockBean CuentaCorrienteServiceImpl cuentaCorrienteService;
  @MockBean MessageSource messageSource;

  @Autowired FacturaServiceImpl facturaServiceImpl;

  @Test
  void shouldGetTiposFacturaWhenSucursalDiscriminaIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    TipoDeComprobante[] expResult = {
      TipoDeComprobante.FACTURA_A,
      TipoDeComprobante.FACTURA_B,
      TipoDeComprobante.FACTURA_X,
      TipoDeComprobante.FACTURA_Y,
      TipoDeComprobante.PRESUPUESTO
    };
    TipoDeComprobante[] result = facturaServiceImpl.getTiposDeComprobanteSegunSucursal(sucursal);
    assertArrayEquals(expResult, result);
  }

  @Test
  void shouldGetTiposFacturaWhenSucursalNoDiscriminaIVA() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    TipoDeComprobante[] expResult = {
      TipoDeComprobante.FACTURA_C,
      TipoDeComprobante.FACTURA_X,
      TipoDeComprobante.FACTURA_Y,
      TipoDeComprobante.PRESUPUESTO
    };
    TipoDeComprobante[] result = facturaServiceImpl.getTiposDeComprobanteSegunSucursal(sucursal);
    assertArrayEquals(expResult, result);
  }

  @Test
  void shouldMarcarRenglonParaAplicarBonificacion() {
    Producto productoParaRetorno = new Producto();
    productoParaRetorno.setBulto(new BigDecimal("5"));
    when(productoService.getProductoNoEliminadoPorId(1L)).thenReturn(productoParaRetorno);
    assertTrue(facturaServiceImpl.marcarRenglonParaAplicarBonificacion(1L, new BigDecimal("5")));
    assertFalse(facturaServiceImpl.marcarRenglonParaAplicarBonificacion(1L, new BigDecimal("3")));
  }

  @Test
  void shouldCalcularRenglon() {
    Producto productoParaRetorno = new Producto();
    productoParaRetorno.setIdProducto(1L);
    productoParaRetorno.setCodigo("1");
    productoParaRetorno.setDescripcion("Producto para test");
    productoParaRetorno.setMedida(new Medida());
    productoParaRetorno.setPrecioCosto(new BigDecimal("89.35"));
    productoParaRetorno.setGananciaPorcentaje(new BigDecimal("38.74"));
    productoParaRetorno.setGananciaNeto(new BigDecimal("34.62"));
    productoParaRetorno.setPrecioVentaPublico(new BigDecimal("123.97"));
    productoParaRetorno.setIvaPorcentaje(new BigDecimal("21"));
    productoParaRetorno.setIvaNeto(new BigDecimal("26.03"));
    productoParaRetorno.setPrecioLista(new BigDecimal("150"));
    productoParaRetorno.setPorcentajeBonificacionPrecio(new BigDecimal("10"));
    productoParaRetorno.setPrecioBonificado(new BigDecimal("135"));
    productoParaRetorno.setPorcentajeBonificacionOferta(BigDecimal.ZERO);
    productoParaRetorno.setBulto(new BigDecimal("5"));
    when(productoService.getProductoNoEliminadoPorId(1L)).thenReturn(productoParaRetorno);
    NuevoRenglonFacturaDTO nuevoRenglonFacturaDTO =
        NuevoRenglonFacturaDTO.builder()
            .renglonMarcado(true)
            .idProducto(1L)
            .cantidad(new BigDecimal("2"))
            .build();
    RenglonFactura renglonFacturaResultante =
        facturaServiceImpl.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaDTO);
    assertEquals(new BigDecimal("223.146000000000000"), renglonFacturaResultante.getImporte());
    nuevoRenglonFacturaDTO.setRenglonMarcado(false);
    renglonFacturaResultante =
        facturaServiceImpl.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaDTO);
    assertEquals(new BigDecimal("247.94"), renglonFacturaResultante.getImporte());
    nuevoRenglonFacturaDTO.setRenglonMarcado(true);
    productoParaRetorno.setOferta(true);
    productoParaRetorno.setPorcentajeBonificacionOferta(new BigDecimal("20"));
    renglonFacturaResultante =
        facturaServiceImpl.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaDTO);
    assertEquals(new BigDecimal("198.352000000000000"), renglonFacturaResultante.getImporte());
    nuevoRenglonFacturaDTO.setRenglonMarcado(false);
    renglonFacturaResultante =
        facturaServiceImpl.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaDTO);
    assertEquals(new BigDecimal("247.94"), renglonFacturaResultante.getImporte());
  }

  @Test
  void shouldCalcularSubTotal() {
    RenglonFactura renglon1 = new RenglonFactura();
    renglon1.setImporte(new BigDecimal("5.601"));
    RenglonFactura renglon2 = new RenglonFactura();
    renglon2.setImporte(new BigDecimal("18.052"));
    RenglonFactura renglon3 = new RenglonFactura();
    renglon3.setImporte(new BigDecimal("10.011"));
    List<RenglonFactura> renglones = new ArrayList<>();
    renglones.add(renglon1);
    renglones.add(renglon2);
    renglones.add(renglon3);
    BigDecimal[] importes = new BigDecimal[renglones.size()];
    int indice = 0;
    for (RenglonFactura renglon : renglones) {
      importes[indice] = renglon.getImporte();
      indice++;
    }
    assertEquals(33.664, CalculosComprobante.calcularSubTotal(importes).doubleValue());
  }

  @Test
  void shouldCacularDescuentoNeto() {
    assertEquals(
        11.773464750000000,
        CalculosComprobante.calcularProporcion(new BigDecimal("78.255"), new BigDecimal("15.045"))
            .doubleValue());
  }

  @Test
  void shouldCalcularRecargoNeto() {
    assertEquals(
        12.11047244,
        CalculosComprobante.calcularProporcion(new BigDecimal("78.122"), new BigDecimal("15.502"))
            .doubleValue());
  }

  @Test
  void shouldCalcularSubTotalBrutoFacturaA() {
    assertEquals(
        220.477,
        CalculosComprobante.calcularSubTotalBruto(
                false,
                new BigDecimal("225.025"),
                new BigDecimal("10.454"),
                new BigDecimal("15.002"),
                BigDecimal.ZERO,
                BigDecimal.ZERO)
            .doubleValue());
  }

  @Test
  void shouldCalcularSubTotalBrutoFacturaB() {
    assertEquals(
        795.2175,
        CalculosComprobante.calcularSubTotalBruto(
                true,
                new BigDecimal("1205.5"),
                new BigDecimal("80.5"),
                new BigDecimal("111.05"),
                new BigDecimal("253.155"),
                new BigDecimal("126.5775"))
            .doubleValue());
  }

  @Test
  void shouldCalcularIva_netoWhenLaFacturaEsA() {
    RenglonFactura renglon1 = new RenglonFactura();
    renglon1.setCantidad(new BigDecimal("12"));
    renglon1.setIvaPorcentaje(new BigDecimal("21"));
    renglon1.setIvaNeto(new BigDecimal("125.5"));
    RenglonFactura renglon2 = new RenglonFactura();
    renglon2.setCantidad(new BigDecimal("8"));
    renglon2.setIvaPorcentaje(new BigDecimal("21"));
    renglon2.setIvaNeto(new BigDecimal("240.2"));
    RenglonFactura renglon3 = new RenglonFactura();
    renglon3.setCantidad(new BigDecimal("4"));
    renglon3.setIvaPorcentaje(new BigDecimal("10.5"));
    renglon3.setIvaNeto(new BigDecimal("110.5"));
    List<RenglonFactura> renglones = new ArrayList<>();
    renglones.add(renglon1);
    renglones.add(renglon2);
    renglones.add(renglon3);
    // El renglon3 no lo deberia tener en cuenta para el calculo ya que NO es 21% de IVA
    int size = renglones.size();
    BigDecimal[] cantidades = new BigDecimal[size];
    BigDecimal[] ivaPorcentajes = new BigDecimal[size];
    BigDecimal[] ivaNetos = new BigDecimal[size];
    int i = 0;
    for (RenglonFactura r : renglones) {
      cantidades[i] = r.getCantidad();
      ivaPorcentajes[i] = r.getIvaPorcentaje();
      ivaNetos[i] = r.getIvaNeto();
      i++;
    }
    assertEquals(
        0,
        facturaServiceImpl
            .calcularIvaNetoFactura(
                TipoDeComprobante.FACTURA_A,
                cantidades,
                ivaPorcentajes,
                ivaNetos,
                new BigDecimal("21"),
                BigDecimal.ZERO,
                BigDecimal.ZERO)
            .compareTo(new BigDecimal("3427.6")));
  }

  @Test
  void shouldCalcularIva_netoWhenLaFacturaEsX() {
    RenglonFactura renglon1 = new RenglonFactura();
    renglon1.setImporte(new BigDecimal("5.601"));
    renglon1.setIvaPorcentaje(BigDecimal.ZERO);
    renglon1.setCantidad(BigDecimal.ONE);
    renglon1.setIvaNeto(new BigDecimal("1.17621"));
    RenglonFactura renglon2 = new RenglonFactura();
    renglon2.setImporte(new BigDecimal("18.052"));
    renglon2.setIvaPorcentaje(BigDecimal.ZERO);
    renglon2.setCantidad(BigDecimal.ONE);
    renglon2.setIvaNeto(new BigDecimal("3.79092"));
    RenglonFactura renglon3 = new RenglonFactura();
    renglon3.setImporte(new BigDecimal("10.011"));
    renglon3.setIvaPorcentaje(BigDecimal.ZERO);
    renglon3.setCantidad(BigDecimal.ONE);
    renglon3.setIvaNeto(new BigDecimal("2.10231"));
    List<RenglonFactura> renglones = new ArrayList<>();
    renglones.add(renglon1);
    renglones.add(renglon2);
    renglones.add(renglon3);
    int size = renglones.size();
    BigDecimal[] cantidades = new BigDecimal[size];
    BigDecimal[] ivaPorcentajes = new BigDecimal[size];
    BigDecimal[] ivaNetos = new BigDecimal[size];
    int i = 0;
    for (RenglonFactura r : renglones) {
      cantidades[i] = r.getCantidad();
      ivaPorcentajes[i] = r.getIvaPorcentaje();
      ivaNetos[i] = r.getIvaNeto();
      i++;
    }
    assertEquals(
        0,
        facturaServiceImpl
            .calcularIvaNetoFactura(
                TipoDeComprobante.FACTURA_X,
                cantidades,
                ivaPorcentajes,
                ivaNetos,
                new BigDecimal("21"),
                BigDecimal.ZERO,
                BigDecimal.ZERO)
            .doubleValue());
  }

  @Test
  void shouldCalcularTotal() {
    assertEquals(
        386.363,
        CalculosComprobante.calcularTotal(
                new BigDecimal("350.451"), new BigDecimal("10.753"), new BigDecimal("25.159"))
            .doubleValue());
  }

  @Test
  void shouldCalcularImporte() {
    assertEquals(
        90,
        CalculosComprobante.calcularImporte(
                new BigDecimal("10"), new BigDecimal("10"), BigDecimal.ONE)
            .doubleValue());
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaA() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        0,
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_A, producto)
            .compareTo(new BigDecimal("121")));
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaX() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        0,
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_X, producto)
            .compareTo(new BigDecimal("121")));
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaA() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        0,
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_A, producto)
            .compareTo(new BigDecimal("100")));
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaX() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        0,
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_X, producto)
            .compareTo(new BigDecimal("100")));
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaB() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        0,
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_B, producto)
            .compareTo(new BigDecimal("121")));
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaC() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        0,
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_C, producto)
            .compareTo(new BigDecimal("121")));
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaY() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        0,
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_Y, producto)
            .compareTo(new BigDecimal("121")));
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaB() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setGananciaNeto(new BigDecimal("100"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    producto.setIvaNeto(new BigDecimal("42"));
    producto.setPrecioVentaPublico(new BigDecimal("200"));
    producto.setPrecioLista(new BigDecimal("242"));
    assertEquals(
        0,
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_B, producto)
            .compareTo(new BigDecimal("242")));
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaC() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setGananciaNeto(new BigDecimal("100"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    producto.setIvaNeto(new BigDecimal("42"));
    producto.setPrecioVentaPublico(new BigDecimal("200"));
    producto.setPrecioLista(new BigDecimal("242"));
    assertEquals(
        0,
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_C, producto)
            .compareTo(new BigDecimal("242")));
  }

  @Test
  void shouldCalcularIVANetoWhenVentaConFacturaA() {
    Producto producto = new Producto();
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        25.41,
        facturaServiceImpl
            .calcularIVANetoRenglon(
                Movimiento.VENTA, TipoDeComprobante.FACTURA_A, producto, BigDecimal.ZERO)
            .doubleValue());
  }

  @Test
  void shouldCalcularIVANetoWhenVentaConFacturaB() {
    Producto producto = new Producto();
    producto.setPrecioVentaPublico(new BigDecimal("1000"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        0,
        facturaServiceImpl
            .calcularIVANetoRenglon(
                Movimiento.VENTA, TipoDeComprobante.FACTURA_B, producto, BigDecimal.ZERO)
            .compareTo(new BigDecimal("210")));
  }

  @Test
  void shouldGetArrayDeIdProductoParaFactura() {
    long[] arrayEsperado = {1L, 2L, 3L};
    List<NuevoRenglonFacturaDTO> nuevosRenglonsFactura = new ArrayList<>();
    nuevosRenglonsFactura.add(NuevoRenglonFacturaDTO.builder().idProducto(1L).build());
    nuevosRenglonsFactura.add(NuevoRenglonFacturaDTO.builder().idProducto(2L).build());
    nuevosRenglonsFactura.add(NuevoRenglonFacturaDTO.builder().idProducto(3L).build());
    assertArrayEquals(
        arrayEsperado, CalculosComprobante.getArrayDeIdProductoParaFactura(nuevosRenglonsFactura));
  }

  @Test
  void shouldGetArrayDeCantidadesParaFactura() {
    BigDecimal[] arrayEsperado = {new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("5")};
    List<NuevoRenglonFacturaDTO> nuevosRenglonsFactura = new ArrayList<>();
    nuevosRenglonsFactura.add(
        NuevoRenglonFacturaDTO.builder().cantidad(new BigDecimal("10")).build());
    nuevosRenglonsFactura.add(
        NuevoRenglonFacturaDTO.builder().cantidad(new BigDecimal("20")).build());
    nuevosRenglonsFactura.add(
        NuevoRenglonFacturaDTO.builder().cantidad(new BigDecimal("5")).build());
    assertArrayEquals(
        arrayEsperado,
        CalculosComprobante.getArrayDeCantidadesProductoParaFactura(nuevosRenglonsFactura));
  }

  @Test
  void shouldGetArrayDeBonificacionesDeRenglonParaFactura() {
    BigDecimal[] arrayEsperado = {new BigDecimal("5"), new BigDecimal("2"), new BigDecimal("12")};
    List<NuevoRenglonFacturaDTO> nuevosRenglonsFactura = new ArrayList<>();
    nuevosRenglonsFactura.add(
        NuevoRenglonFacturaDTO.builder().bonificacion(new BigDecimal("5")).build());
    nuevosRenglonsFactura.add(
        NuevoRenglonFacturaDTO.builder().bonificacion(new BigDecimal("2")).build());
    nuevosRenglonsFactura.add(
        NuevoRenglonFacturaDTO.builder().bonificacion(new BigDecimal("12")).build());
    assertArrayEquals(
        arrayEsperado,
        CalculosComprobante.getArrayDeBonificacionesParaFactura(nuevosRenglonsFactura));
  }
}
