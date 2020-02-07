package sic.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.builder.ClienteBuilder;
import sic.builder.SucursalBuilder;
import sic.builder.TransportistaBuilder;
import sic.modelo.*;
import sic.repository.FacturaVentaRepository;
import sic.service.IFacturaService;
import sic.util.CalculosComprobante;

@ExtendWith(SpringExtension.class)
class FacturaServiceImplTest {

  @Mock private FacturaVentaRepository facturaVentaRepository;
  @Mock private ProductoServiceImpl productoService;
  @Mock private IFacturaService facturaService;
  @InjectMocks private FacturaServiceImpl facturaServiceImpl;
  @InjectMocks private FacturaCompraServiceImpl facturaCompraService;
  @InjectMocks private FacturaVentaServiceImpl facturaVentaService;

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
    TipoDeComprobante[] result = facturaCompraService.getTiposDeComprobanteCompra(sucursal, proveedor);
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
    TipoDeComprobante[] result = facturaCompraService.getTiposDeComprobanteCompra(sucursal, proveedor);
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
    TipoDeComprobante[] result = facturaCompraService.getTiposDeComprobanteCompra(sucursal, proveedor);
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
    TipoDeComprobante[] result = facturaCompraService.getTiposDeComprobanteCompra(sucursal, proveedor);
    assertArrayEquals(expResult, result);
  }

  @Test
  void shouldGetTipoFacturaVentaWhenSucursalDiscriminaYClienteTambien() {
    Sucursal sucursal = Mockito.mock(Sucursal.class);
    Cliente cliente = Mockito.mock(Cliente.class);
    when(sucursal.getCategoriaIVA()).thenReturn(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    when(cliente.getCategoriaIVA()).thenReturn(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    TipoDeComprobante[] expResult = {
      TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_X, TipoDeComprobante.PRESUPUESTO
    };
    TipoDeComprobante[] result = facturaVentaService.getTiposDeComprobanteVenta(sucursal, cliente);
    assertArrayEquals(expResult, result);
  }

  @Test
  void shouldGetTipoFacturaVentaWhenSucursalDiscriminaYClienteNo() {
    Sucursal sucursal = Mockito.mock(Sucursal.class);
    Cliente cliente = Mockito.mock(Cliente.class);
    when(sucursal.getCategoriaIVA()).thenReturn(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    when(cliente.getCategoriaIVA()).thenReturn(CategoriaIVA.MONOTRIBUTO);
    TipoDeComprobante[] expResult = {
      TipoDeComprobante.FACTURA_B, TipoDeComprobante.FACTURA_X, TipoDeComprobante.PRESUPUESTO
    };
    TipoDeComprobante[] result = facturaVentaService.getTiposDeComprobanteVenta(sucursal, cliente);
    assertArrayEquals(expResult, result);
  }

  @Test
  void shouldGetTipoFacturaVentaWhenSucursalNoDiscriminaYClienteSi() {
    Sucursal sucursal = Mockito.mock(Sucursal.class);
    Cliente cliente = Mockito.mock(Cliente.class);
    when(sucursal.getCategoriaIVA()).thenReturn(CategoriaIVA.MONOTRIBUTO);
    when(cliente.getCategoriaIVA()).thenReturn(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    TipoDeComprobante[] expResult = {
      TipoDeComprobante.FACTURA_C, TipoDeComprobante.FACTURA_X, TipoDeComprobante.PRESUPUESTO
    };
    TipoDeComprobante[] result = facturaVentaService.getTiposDeComprobanteVenta(sucursal, cliente);
    assertArrayEquals(expResult, result);
  }

  @Test
  void shouldGetTipoFacturaVentaWhenSucursalNoDiscriminaIVAYClienteNO() {
    Sucursal sucursal = Mockito.mock(Sucursal.class);
    Cliente cliente = Mockito.mock(Cliente.class);
    when(sucursal.getCategoriaIVA()).thenReturn(CategoriaIVA.MONOTRIBUTO);
    when(cliente.getCategoriaIVA()).thenReturn(CategoriaIVA.MONOTRIBUTO);
    TipoDeComprobante[] expResult = {
      TipoDeComprobante.FACTURA_C, TipoDeComprobante.FACTURA_X, TipoDeComprobante.PRESUPUESTO
    };
    TipoDeComprobante[] result = facturaVentaService.getTiposDeComprobanteVenta(sucursal, cliente);
    assertArrayEquals(expResult, result);
  }

  @Test
  void shouldGetTiposFacturaWhenSucursalDiscriminaIVA() {
    Sucursal sucursal = Mockito.mock(Sucursal.class);
    when(sucursal.getCategoriaIVA()).thenReturn(CategoriaIVA.RESPONSABLE_INSCRIPTO);
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
    Sucursal sucursal = Mockito.mock(Sucursal.class);
    when(sucursal.getCategoriaIVA()).thenReturn(CategoriaIVA.MONOTRIBUTO);
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
  void shouldDividirFactura() {
    when(facturaVentaRepository.buscarMayorNumFacturaSegunTipo(
            TipoDeComprobante.FACTURA_X, 1L, new SucursalBuilder().build().getIdSucursal()))
        .thenReturn(1L);
    when(facturaVentaRepository.buscarMayorNumFacturaSegunTipo(
            TipoDeComprobante.FACTURA_A, 1L, new SucursalBuilder().build().getIdSucursal()))
        .thenReturn(1L);
    RenglonFactura renglon1 = Mockito.mock(RenglonFactura.class);
    RenglonFactura renglon2 = Mockito.mock(RenglonFactura.class);
    RenglonFactura renglon3 = Mockito.mock(RenglonFactura.class);
    RenglonFactura renglon4 = Mockito.mock(RenglonFactura.class);
    RenglonFactura renglon5 = Mockito.mock(RenglonFactura.class);
    RenglonFactura renglon6 = Mockito.mock(RenglonFactura.class);
    Producto producto1 = Mockito.mock(Producto.class);
    Producto producto2 = Mockito.mock(Producto.class);
    Producto producto3 = Mockito.mock(Producto.class);
    Producto producto4 = Mockito.mock(Producto.class);
    Producto producto5 = Mockito.mock(Producto.class);
    Producto producto6 = Mockito.mock(Producto.class);
    Medida medida = Mockito.mock(Medida.class);
    when(producto1.getIdProducto()).thenReturn(1L);
    when(producto1.getCodigo()).thenReturn("1");
    when(producto1.getDescripcion()).thenReturn("producto uno test");
    when(producto1.getMedida()).thenReturn(medida);
    when(producto1.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto1.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto1.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto1.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(productoService.getProductoNoEliminadoPorId(1L)).thenReturn(producto1);
    when(producto2.getIdProducto()).thenReturn(2L);
    when(producto2.getCodigo()).thenReturn("2");
    when(producto2.getDescripcion()).thenReturn("producto dos test");
    when(producto2.getMedida()).thenReturn(medida);
    when(producto2.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto2.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto2.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto2.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(productoService.getProductoNoEliminadoPorId(2L)).thenReturn(producto2);
    when(producto3.getIdProducto()).thenReturn(3L);
    when(producto3.getCodigo()).thenReturn("3");
    when(producto3.getDescripcion()).thenReturn("producto tres test");
    when(producto3.getMedida()).thenReturn(medida);
    when(producto3.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto3.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto3.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto3.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(productoService.getProductoNoEliminadoPorId(3L)).thenReturn(producto3);
    when(producto4.getIdProducto()).thenReturn(4L);
    when(producto4.getCodigo()).thenReturn("4");
    when(producto4.getDescripcion()).thenReturn("producto cuatro test");
    when(producto4.getMedida()).thenReturn(medida);
    when(producto4.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto4.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto4.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto4.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(productoService.getProductoNoEliminadoPorId(4L)).thenReturn(producto4);
    when(producto5.getIdProducto()).thenReturn(5L);
    when(producto5.getCodigo()).thenReturn("5");
    when(producto5.getDescripcion()).thenReturn("producto cinco test");
    when(producto5.getMedida()).thenReturn(medida);
    when(producto5.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto5.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto5.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto5.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(productoService.getProductoNoEliminadoPorId(5L)).thenReturn(producto5);
    when(producto6.getIdProducto()).thenReturn(6L);
    when(producto6.getCodigo()).thenReturn("6");
    when(producto6.getDescripcion()).thenReturn("producto seis test");
    when(producto6.getMedida()).thenReturn(medida);
    when(producto6.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto6.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto6.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto6.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(productoService.getProductoNoEliminadoPorId(6L)).thenReturn(producto6);
    when(renglon1.getIdProductoItem()).thenReturn(1L);
    when(renglon1.getIvaNeto()).thenReturn(new BigDecimal("21"));
    when(renglon1.getBonificacionPorcentaje()).thenReturn(BigDecimal.ZERO);
    when(renglon1.getCantidad()).thenReturn(new BigDecimal("4.00"));
    when(renglon2.getIdProductoItem()).thenReturn(2L);
    when(renglon2.getIvaNeto()).thenReturn(new BigDecimal("10.5"));
    when(renglon2.getBonificacionPorcentaje()).thenReturn(BigDecimal.ZERO);
    when(renglon2.getCantidad()).thenReturn(new BigDecimal("7.00"));
    when(renglon3.getIdProductoItem()).thenReturn(3L);
    when(renglon3.getIvaNeto()).thenReturn(new BigDecimal("21"));
    when(renglon3.getBonificacionPorcentaje()).thenReturn(BigDecimal.ZERO);
    when(renglon3.getCantidad()).thenReturn(new BigDecimal("12.8"));
    when(renglon4.getIdProductoItem()).thenReturn(4L);
    when(renglon4.getIvaNeto()).thenReturn(new BigDecimal("21"));
    when(renglon4.getBonificacionPorcentaje()).thenReturn(BigDecimal.ZERO);
    when(renglon4.getCantidad()).thenReturn(new BigDecimal("1.2"));
    when(renglon5.getIdProductoItem()).thenReturn(5L);
    when(renglon5.getIvaNeto()).thenReturn(new BigDecimal("21"));
    when(renglon5.getBonificacionPorcentaje()).thenReturn(BigDecimal.ZERO);
    when(renglon5.getCantidad()).thenReturn(new BigDecimal("0.8"));
    when(renglon6.getIdProductoItem()).thenReturn(6L);
    when(renglon6.getIvaNeto()).thenReturn(new BigDecimal("21"));
    when(renglon6.getBonificacionPorcentaje()).thenReturn(BigDecimal.ZERO);
    when(renglon6.getCantidad()).thenReturn(new BigDecimal("9.3"));
    List<RenglonFactura> renglones = new ArrayList<>();
    renglones.add(renglon1);
    renglones.add(renglon2);
    renglones.add(renglon3);
    renglones.add(renglon6); // no participa de la division
    renglones.add(renglon4);
    renglones.add(renglon5); // no participa de la division
    FacturaVenta factura = new FacturaVenta();
    factura.setDescuentoPorcentaje(BigDecimal.ZERO);
    factura.setRecargoPorcentaje(BigDecimal.ZERO);
    factura.setRenglones(renglones);
    factura.setFecha(LocalDateTime.now());
    factura.setTransportista(new TransportistaBuilder().build());
    factura.setSucursal(new SucursalBuilder().build());
    factura.setCliente(new ClienteBuilder().withIdCliente(1L).build());
    Usuario usuario = new Usuario();
    usuario.setNombre("Marian Jhons  help");
    factura.setUsuario(usuario);
    factura.setTipoComprobante(TipoDeComprobante.FACTURA_A);
    int[] indices = {0, 1, 2, 4};
    int cantidadDeFacturasEsperadas = 2;
    int cantidadDeRenglonesEsperadosFX = 4;
    int cantidadDeRenglonesEsperadosFA = 6;
    RenglonFactura renglonCalculadoX1 =
        facturaServiceImpl.calcularRenglon(
            TipoDeComprobante.FACTURA_X, Movimiento.VENTA, new BigDecimal("2.000000000000000"), 1, null);
    RenglonFactura renglonCalculadoX2 =
            facturaServiceImpl.calcularRenglon(
                    TipoDeComprobante.FACTURA_X, Movimiento.VENTA, new BigDecimal("3.00"), 2, null);
    RenglonFactura renglonCalculadoX3 =
            facturaServiceImpl.calcularRenglon(
                    TipoDeComprobante.FACTURA_X, Movimiento.VENTA, new BigDecimal("6.400000000000000"), 3, null);
    RenglonFactura renglonCalculadoX4 =
            facturaServiceImpl.calcularRenglon(
                    TipoDeComprobante.FACTURA_X, Movimiento.VENTA, new BigDecimal("0.600000000000000"), 4, null);
    RenglonFactura renglonCalculadoA1 =
            facturaServiceImpl.calcularRenglon(
                    TipoDeComprobante.FACTURA_X, Movimiento.VENTA, new BigDecimal("2.000000000000000"), 1, null);
    RenglonFactura renglonCalculadoA2 =
            facturaServiceImpl.calcularRenglon(
                    TipoDeComprobante.FACTURA_A, Movimiento.VENTA, new BigDecimal("4"), 2, null);
    RenglonFactura renglonCalculadoA3 =
            facturaServiceImpl.calcularRenglon(
                    TipoDeComprobante.FACTURA_A, Movimiento.VENTA, new BigDecimal("6.400000000000000"), 3, null);
    RenglonFactura renglonCalculadoA4 =
            facturaServiceImpl.calcularRenglon(
                    TipoDeComprobante.FACTURA_A, Movimiento.VENTA, new BigDecimal("9.3"), 6, null);
    RenglonFactura renglonCalculadoA5 =
            facturaServiceImpl.calcularRenglon(
                    TipoDeComprobante.FACTURA_A, Movimiento.VENTA, new BigDecimal("0.600000000000000"), 4, null);
    RenglonFactura renglonCalculadoA6 =
            facturaServiceImpl.calcularRenglon(
                    TipoDeComprobante.FACTURA_A, Movimiento.VENTA, new BigDecimal("0.8"), 5, null);
    when(facturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_X, Movimiento.VENTA, new BigDecimal("2.000000000000000"), 1, null))
        .thenReturn(renglonCalculadoX1);
    when(facturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_X, Movimiento.VENTA, new BigDecimal("3.00"), 2, null))
            .thenReturn(renglonCalculadoX2);
    when(facturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_X, Movimiento.VENTA, new BigDecimal("6.400000000000000"), 3, null))
            .thenReturn(renglonCalculadoX3);
    when(facturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_X, Movimiento.VENTA, new BigDecimal("0.600000000000000"), 4, null))
            .thenReturn(renglonCalculadoX4);
    when(facturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, new BigDecimal("2.000000000000000"), 1, null))
            .thenReturn(renglonCalculadoA1);
    when(facturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, new BigDecimal("4"), 2, null))
            .thenReturn(renglonCalculadoA2);
    when(facturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, new BigDecimal("6.400000000000000"), 3, null))
            .thenReturn(renglonCalculadoA3);
    when(facturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, new BigDecimal("9.3"), 6, null))
            .thenReturn(renglonCalculadoA4);
    when(facturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, new BigDecimal("0.600000000000000"), 4, null))
            .thenReturn(renglonCalculadoA5);
    when(facturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, new BigDecimal("0.8"), 5, null))
            .thenReturn(renglonCalculadoA6);
    List<FacturaVenta> result = facturaVentaService.dividirFactura(factura, indices);
    assertEquals(cantidadDeFacturasEsperadas, result.size());
    assertEquals(cantidadDeRenglonesEsperadosFX, result.get(0).getRenglones().size());
    assertEquals(cantidadDeRenglonesEsperadosFA, result.get(1).getRenglones().size());
    BigDecimal cantidadPrimerRenglonFacturaX = result.get(0).getRenglones().get(0).getCantidad();
    BigDecimal cantidadSegundoRenglonFacturaX = result.get(0).getRenglones().get(1).getCantidad();
    BigDecimal cantidadTercerRenglonFacturaX = result.get(0).getRenglones().get(2).getCantidad();
    BigDecimal cantidadCuartoRenglonFacturaX = result.get(0).getRenglones().get(3).getCantidad();
    BigDecimal cantidadPrimerRenglonFacturaA = result.get(1).getRenglones().get(0).getCantidad();
    BigDecimal cantidadSegundoRenglonFacturaA = result.get(1).getRenglones().get(1).getCantidad();
    BigDecimal cantidadTercerRenglonFacturaA = result.get(1).getRenglones().get(2).getCantidad();
    BigDecimal cantidadCuartoRenglonFacturaA = result.get(1).getRenglones().get(3).getCantidad();
    BigDecimal cantidadQuintoRenglonFacturaA = result.get(1).getRenglones().get(4).getCantidad();
    BigDecimal cantidadSextoRenglonFacturaA = result.get(1).getRenglones().get(5).getCantidad();
    assertEquals(cantidadPrimerRenglonFacturaA.compareTo(new BigDecimal("2")), 0);
    assertEquals(cantidadSegundoRenglonFacturaA.compareTo(new BigDecimal("4")), 0);
    assertEquals(cantidadTercerRenglonFacturaA.compareTo(new BigDecimal("6.4")), 0);
    assertEquals(cantidadCuartoRenglonFacturaA.compareTo(new BigDecimal("9.3")), 0);
    assertEquals(cantidadQuintoRenglonFacturaA.compareTo(new BigDecimal("0.6")), 0);
    assertEquals(cantidadSextoRenglonFacturaA.compareTo(new BigDecimal("0.8")), 0);
    assertEquals(cantidadPrimerRenglonFacturaX.compareTo(new BigDecimal("2")), 0);
    assertEquals(cantidadSegundoRenglonFacturaX.compareTo(new BigDecimal("3")), 0);
    assertEquals(cantidadTercerRenglonFacturaX.compareTo(new BigDecimal("6.4")), 0);
    assertEquals(cantidadCuartoRenglonFacturaX.compareTo(new BigDecimal("0.6")), 0);
  }

  // Calculos
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
    assertEquals(CalculosComprobante.calcularSubTotal(importes).doubleValue(), 33.664);
  }

  @Test
  void shouldCacularDescuentoNeto() {
    assertEquals(
        CalculosComprobante.calcularProporcion(new BigDecimal("78.255"), new BigDecimal("15.045"))
            .doubleValue(),
        11.773464750000000);
  }

  @Test
  void shouldCalcularRecargoNeto() {
    assertEquals(
        CalculosComprobante.calcularProporcion(new BigDecimal("78.122"), new BigDecimal("15.502"))
            .doubleValue(),
        12.11047244);
  }

  @Test
  void shouldCalcularSubTotalBrutoFacturaA() {
    assertEquals(
        CalculosComprobante.calcularSubTotalBruto(
                false,
                new BigDecimal("225.025"),
                new BigDecimal("10.454"),
                new BigDecimal("15.002"),
                BigDecimal.ZERO,
                BigDecimal.ZERO)
            .doubleValue(),
        220.477);
  }

  @Test
  void shouldCalcularSubTotalBrutoFacturaB() {
    assertEquals(
        CalculosComprobante.calcularSubTotalBruto(
                true,
                new BigDecimal("1205.5"),
                new BigDecimal("80.5"),
                new BigDecimal("111.05"),
                new BigDecimal("253.155"),
                new BigDecimal("126.5775"))
            .doubleValue(),
        795.2175);
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
        facturaServiceImpl
            .calcularIvaNetoFactura(
                TipoDeComprobante.FACTURA_A,
                cantidades,
                ivaPorcentajes,
                ivaNetos,
                new BigDecimal("21"),
                BigDecimal.ZERO,
                BigDecimal.ZERO)
            .compareTo(new BigDecimal("3427.6")),
        0);
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
        facturaServiceImpl
            .calcularIvaNetoFactura(
                TipoDeComprobante.FACTURA_X,
                cantidades,
                ivaPorcentajes,
                ivaNetos,
                new BigDecimal("21"),
                BigDecimal.ZERO,
                BigDecimal.ZERO)
            .doubleValue(),
        0);
  }

  @Test
  void shouldCalcularTotal() {
    assertEquals(
        CalculosComprobante.calcularTotal(
                new BigDecimal("350.451"), new BigDecimal("10.753"), new BigDecimal("25.159"))
            .doubleValue(),
        386.363);
  }

  @Test
  void shouldCalcularImporte() {
    assertEquals(
        CalculosComprobante.calcularImporte(
                new BigDecimal("10"), new BigDecimal("10"), BigDecimal.ONE)
            .doubleValue(),
        90);
  }

  @Test
  void shouldCalcularIVANetoWhenCompraConFacturaA() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        facturaServiceImpl
            .calcularIVANetoRenglon(
                Movimiento.COMPRA, TipoDeComprobante.FACTURA_A, producto, BigDecimal.ZERO)
            .compareTo(new BigDecimal("21")),
        0);
  }

  @Test
  void shouldCalcularIVANetoWhenCompraConFacturaB() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("200"));
    producto.setPrecioVentaPublico(new BigDecimal("1000"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        facturaServiceImpl
            .calcularIVANetoRenglon(
                Movimiento.COMPRA, TipoDeComprobante.FACTURA_B, producto, BigDecimal.ZERO)
            .doubleValue(),
        42);
  }

  @Test
  void shouldCalcularIVANetoWhenVentaConFacturaA() {
    Producto producto = new Producto();
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        facturaServiceImpl
            .calcularIVANetoRenglon(
                Movimiento.VENTA, TipoDeComprobante.FACTURA_A, producto, BigDecimal.ZERO)
            .doubleValue(),
        25.41);
  }

  @Test
  void shouldCalcularIVANetoWhenVentaConFacturaB() {
    Producto producto = new Producto();
    producto.setPrecioVentaPublico(new BigDecimal("1000"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        facturaServiceImpl
            .calcularIVANetoRenglon(
                Movimiento.VENTA, TipoDeComprobante.FACTURA_B, producto, BigDecimal.ZERO)
            .compareTo(new BigDecimal("210")),
        0);
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaA() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_A, producto)
            .compareTo(new BigDecimal("121")),
        0);
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaX() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_X, producto)
            .compareTo(new BigDecimal("121")),
        0);
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaA() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_A, producto)
            .compareTo(new BigDecimal("100")),
        0);
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaX() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_X, producto)
            .compareTo(new BigDecimal("100")),
        0);
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaB() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_B, producto)
            .compareTo(new BigDecimal("121")),
        0);
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaC() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_C, producto)
            .compareTo(new BigDecimal("121")),
        0);
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaY() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_Y, producto)
            .compareTo(new BigDecimal("121")),
        0);
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
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_B, producto)
            .compareTo(new BigDecimal("242")),
        0);
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
        facturaServiceImpl
            .calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_C, producto)
            .compareTo(new BigDecimal("242")),
        0);
  }
}
