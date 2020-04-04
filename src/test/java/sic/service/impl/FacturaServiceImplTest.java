package sic.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import com.querydsl.core.BooleanBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaFacturaCompraCriteria;
import sic.modelo.criteria.BusquedaFacturaVentaCriteria;
import sic.modelo.dto.NuevoRenglonFacturaDTO;
import sic.modelo.dto.UbicacionDTO;
import sic.repository.FacturaCompraRepository;
import sic.repository.FacturaVentaRepository;
import sic.service.IFacturaService;
import sic.util.CalculosComprobante;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppTest.class)
class FacturaServiceImplTest {

  @Autowired
  MessageSource messageSourceTest;

  @Mock FacturaVentaRepository mockFacturaVentaRepository;
  @Mock FacturaCompraRepository mockFacturaCompraRepository;
  @Mock ProductoServiceImpl mockProductoService;
  @Mock IFacturaService mockFacturaService;
  @Mock MessageSource messageSourceTestMock;
  @Mock UsuarioServiceImpl mockUsuarioService;
  @Mock ClienteServiceImpl mockClienteService;
  @Mock PedidoServiceImpl pedidoService;
  @Mock ConfiguracionSucursalServiceImpl mockConfiguracionSucursalService;
  @Mock CorreoElectronicoServiceImpl mockCorreoElectronicoService;
  @InjectMocks FacturaServiceImpl facturaServiceImpl;
  @InjectMocks FacturaCompraServiceImpl facturaCompraServiceImpl;
  @InjectMocks FacturaVentaServiceImpl facturaVentaServiceImpl;

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
    TipoDeComprobante[] result = facturaCompraServiceImpl.getTiposDeComprobanteCompra(sucursal, proveedor);
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
    TipoDeComprobante[] result = facturaCompraServiceImpl.getTiposDeComprobanteCompra(sucursal, proveedor);
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
    TipoDeComprobante[] result = facturaCompraServiceImpl.getTiposDeComprobanteCompra(sucursal, proveedor);
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
    TipoDeComprobante[] result = facturaCompraServiceImpl.getTiposDeComprobanteCompra(sucursal, proveedor);
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
    TipoDeComprobante[] result = facturaVentaServiceImpl.getTiposDeComprobanteVenta(sucursal, cliente);
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
    TipoDeComprobante[] result = facturaVentaServiceImpl.getTiposDeComprobanteVenta(sucursal, cliente);
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
    TipoDeComprobante[] result = facturaVentaServiceImpl.getTiposDeComprobanteVenta(sucursal, cliente);
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
    TipoDeComprobante[] result = facturaVentaServiceImpl.getTiposDeComprobanteVenta(sucursal, cliente);
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
    when(mockFacturaVentaRepository.buscarMayorNumFacturaSegunTipo(
            TipoDeComprobante.FACTURA_X, 1L, 1L))
        .thenReturn(1L);
    when(mockFacturaVentaRepository.buscarMayorNumFacturaSegunTipo(
            TipoDeComprobante.FACTURA_A, 1L, 1L))
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
    when(mockProductoService.getProductoNoEliminadoPorId(1L)).thenReturn(producto1);
    when(producto2.getIdProducto()).thenReturn(2L);
    when(producto2.getCodigo()).thenReturn("2");
    when(producto2.getDescripcion()).thenReturn("producto dos test");
    when(producto2.getMedida()).thenReturn(medida);
    when(producto2.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto2.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto2.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto2.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(mockProductoService.getProductoNoEliminadoPorId(2L)).thenReturn(producto2);
    when(producto3.getIdProducto()).thenReturn(3L);
    when(producto3.getCodigo()).thenReturn("3");
    when(producto3.getDescripcion()).thenReturn("producto tres test");
    when(producto3.getMedida()).thenReturn(medida);
    when(producto3.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto3.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto3.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto3.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(mockProductoService.getProductoNoEliminadoPorId(3L)).thenReturn(producto3);
    when(producto4.getIdProducto()).thenReturn(4L);
    when(producto4.getCodigo()).thenReturn("4");
    when(producto4.getDescripcion()).thenReturn("producto cuatro test");
    when(producto4.getMedida()).thenReturn(medida);
    when(producto4.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto4.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto4.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto4.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(mockProductoService.getProductoNoEliminadoPorId(4L)).thenReturn(producto4);
    when(producto5.getIdProducto()).thenReturn(5L);
    when(producto5.getCodigo()).thenReturn("5");
    when(producto5.getDescripcion()).thenReturn("producto cinco test");
    when(producto5.getMedida()).thenReturn(medida);
    when(producto5.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto5.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto5.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto5.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(mockProductoService.getProductoNoEliminadoPorId(5L)).thenReturn(producto5);
    when(producto6.getIdProducto()).thenReturn(6L);
    when(producto6.getCodigo()).thenReturn("6");
    when(producto6.getDescripcion()).thenReturn("producto seis test");
    when(producto6.getMedida()).thenReturn(medida);
    when(producto6.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto6.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto6.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto6.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(mockProductoService.getProductoNoEliminadoPorId(6L)).thenReturn(producto6);
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
    factura.setTransportista(new Transportista());
    factura.setSucursal(new Sucursal());
    Cliente cliente = new Cliente();
    cliente.setIdCliente(1L);
    factura.setCliente(cliente);
    Usuario usuario = new Usuario();
    usuario.setNombre("Marian Jhons  help");
    factura.setUsuario(usuario);
    factura.setTipoComprobante(TipoDeComprobante.FACTURA_A);
    int[] indices = {0, 1, 2, 4};
    int cantidadDeFacturasEsperadas = 2;
    int cantidadDeRenglonesEsperadosFX = 4;
    int cantidadDeRenglonesEsperadosFA = 6;
    NuevoRenglonFacturaDTO nuevoRenglonFacturaX1 =
        NuevoRenglonFacturaDTO.builder()
            .idProducto(1L)
            .cantidad(new BigDecimal("2.000000000000000"))
            .build();
    RenglonFactura renglonCalculadoX1 =
        facturaServiceImpl.calcularRenglon(
            TipoDeComprobante.FACTURA_X, Movimiento.VENTA, nuevoRenglonFacturaX1);
    NuevoRenglonFacturaDTO nuevoRenglonFacturaX2 =
        NuevoRenglonFacturaDTO.builder().idProducto(2L).cantidad(new BigDecimal("3.00")).build();
    RenglonFactura renglonCalculadoX2 =
        facturaServiceImpl.calcularRenglon(
            TipoDeComprobante.FACTURA_X, Movimiento.VENTA, nuevoRenglonFacturaX2);
    NuevoRenglonFacturaDTO nuevoRenglonFacturaX3 =
        NuevoRenglonFacturaDTO.builder()
            .idProducto(3L)
            .cantidad(new BigDecimal("6.400000000000000"))
            .build();
    RenglonFactura renglonCalculadoX3 =
        facturaServiceImpl.calcularRenglon(
            TipoDeComprobante.FACTURA_X, Movimiento.VENTA, nuevoRenglonFacturaX3);
    NuevoRenglonFacturaDTO nuevoRenglonFacturaX4 =
        NuevoRenglonFacturaDTO.builder()
            .idProducto(4L)
            .cantidad(new BigDecimal("0.600000000000000"))
            .build();
    RenglonFactura renglonCalculadoX4 =
        facturaServiceImpl.calcularRenglon(
            TipoDeComprobante.FACTURA_X, Movimiento.VENTA, nuevoRenglonFacturaX4);
    NuevoRenglonFacturaDTO nuevoRenglonFacturaA1 =
        NuevoRenglonFacturaDTO.builder()
            .idProducto(1L)
            .cantidad(new BigDecimal("2.000000000000000"))
            .build();
    RenglonFactura renglonCalculadoA1 =
            facturaServiceImpl.calcularRenglon(
                    TipoDeComprobante.FACTURA_X, Movimiento.VENTA, nuevoRenglonFacturaA1);
    NuevoRenglonFacturaDTO nuevoRenglonFacturaA2 =
        NuevoRenglonFacturaDTO.builder().idProducto(2L).cantidad(new BigDecimal("4")).build();
    RenglonFactura renglonCalculadoA2 =
            facturaServiceImpl.calcularRenglon(
                    TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaA2);
    NuevoRenglonFacturaDTO nuevoRenglonFacturaA3 =
            NuevoRenglonFacturaDTO.builder().idProducto(3L).cantidad(new BigDecimal("6.400000000000000")).build();
    RenglonFactura renglonCalculadoA3 =
        facturaServiceImpl.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaA3);
    NuevoRenglonFacturaDTO nuevoRenglonFacturaA4 =
            NuevoRenglonFacturaDTO.builder().idProducto(6L).cantidad(new BigDecimal("9.3")).build();
    RenglonFactura renglonCalculadoA4 =
        facturaServiceImpl.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaA4);
    NuevoRenglonFacturaDTO nuevoRenglonFacturaA5 =
        NuevoRenglonFacturaDTO.builder()
            .idProducto(4L)
            .cantidad(new BigDecimal("0.600000000000000"))
            .build();
    RenglonFactura renglonCalculadoA5 =
        facturaServiceImpl.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaA5);
    NuevoRenglonFacturaDTO nuevoRenglonFacturaA6 =
            NuevoRenglonFacturaDTO.builder()
                    .idProducto(5L)
                    .cantidad(new BigDecimal("0.8"))
                    .build();
    RenglonFactura renglonCalculadoA6 =
        facturaServiceImpl.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaA6);
    when(mockFacturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_X, Movimiento.VENTA, nuevoRenglonFacturaX1))
        .thenReturn(renglonCalculadoX1);
    when(mockFacturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_X, Movimiento.VENTA, nuevoRenglonFacturaX2))
            .thenReturn(renglonCalculadoX2);
    when(mockFacturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_X, Movimiento.VENTA, nuevoRenglonFacturaX3))
            .thenReturn(renglonCalculadoX3);
    when(mockFacturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_X, Movimiento.VENTA, nuevoRenglonFacturaX4))
            .thenReturn(renglonCalculadoX4);
    when(mockFacturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaA1))
            .thenReturn(renglonCalculadoA1);
    when(mockFacturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaA2))
            .thenReturn(renglonCalculadoA2);
    when(mockFacturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaA3))
            .thenReturn(renglonCalculadoA3);
    when(mockFacturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaA4))
            .thenReturn(renglonCalculadoA4);
    when(mockFacturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaA5))
            .thenReturn(renglonCalculadoA5);
    when(mockFacturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaA6))
            .thenReturn(renglonCalculadoA6);
    when(mockFacturaService.calcularIvaNetoFactura(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(BigDecimal.ZERO);
    List<FacturaVenta> result = facturaVentaServiceImpl.dividirFactura(factura, indices);
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
    assertEquals(0, cantidadPrimerRenglonFacturaA.compareTo(new BigDecimal("2")));
    assertEquals(0, cantidadSegundoRenglonFacturaA.compareTo(new BigDecimal("4")));
    assertEquals(0, cantidadTercerRenglonFacturaA.compareTo(new BigDecimal("6.4")));
    assertEquals(0, cantidadCuartoRenglonFacturaA.compareTo(new BigDecimal("9.3")));
    assertEquals(0, cantidadQuintoRenglonFacturaA.compareTo(new BigDecimal("0.6")));
    assertEquals(0, cantidadSextoRenglonFacturaA.compareTo(new BigDecimal("0.8")));
    assertEquals(0, cantidadPrimerRenglonFacturaX.compareTo(new BigDecimal("2")));
    assertEquals(0, cantidadSegundoRenglonFacturaX.compareTo(new BigDecimal("3")));
    assertEquals(0, cantidadTercerRenglonFacturaX.compareTo(new BigDecimal("6.4")));
    assertEquals(0, cantidadCuartoRenglonFacturaX.compareTo(new BigDecimal("0.6")));
  }

  @Test
  void shouldMarcarRenglonParaAplicarBonificacion() {
    Producto productoParaRetorno = new Producto();
    productoParaRetorno.setBulto(new BigDecimal("5"));
    when(mockProductoService.getProductoNoEliminadoPorId(1L)).thenReturn(productoParaRetorno);
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
    when(mockProductoService.getProductoNoEliminadoPorId(1L)).thenReturn(productoParaRetorno);
    NuevoRenglonFacturaDTO nuevoRenglonFacturaDTO = NuevoRenglonFacturaDTO.builder()
            .renglonMarcado(true)
            .idProducto(1L)
            .cantidad(new BigDecimal("2"))
            .build();
    RenglonFactura renglonFacturaResultante = facturaServiceImpl.calcularRenglon(TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaDTO);
    assertEquals(new BigDecimal("223.146000000000000"), renglonFacturaResultante.getImporte());
    nuevoRenglonFacturaDTO.setRenglonMarcado(false);
    renglonFacturaResultante = facturaServiceImpl.calcularRenglon(TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaDTO);
    assertEquals(new BigDecimal("247.94"), renglonFacturaResultante.getImporte());
    nuevoRenglonFacturaDTO.setRenglonMarcado(true);
    productoParaRetorno.setOferta(true);
    productoParaRetorno.setPorcentajeBonificacionOferta(new BigDecimal("20"));
    renglonFacturaResultante = facturaServiceImpl.calcularRenglon(TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaDTO);
    assertEquals(new BigDecimal("198.352000000000000"), renglonFacturaResultante.getImporte());
    nuevoRenglonFacturaDTO.setRenglonMarcado(false);
    renglonFacturaResultante = facturaServiceImpl.calcularRenglon(TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevoRenglonFacturaDTO);
    assertEquals(new BigDecimal("247.94"), renglonFacturaResultante.getImporte());
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
    assertEquals(0,
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
    assertEquals(0,
            facturaServiceImpl.calcularIvaNetoFactura(
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
  void shouldCalcularIVANetoWhenCompraConFacturaA() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(0,
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
    assertEquals(42,
        facturaServiceImpl
            .calcularIVANetoRenglon(
                Movimiento.COMPRA, TipoDeComprobante.FACTURA_B, producto, BigDecimal.ZERO)
            .doubleValue());
  }

  @Test
  void shouldCalcularIVANetoWhenVentaConFacturaA() {
    Producto producto = new Producto();
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(25.41,
        facturaServiceImpl.calcularIVANetoRenglon(
                Movimiento.VENTA, TipoDeComprobante.FACTURA_A, producto, BigDecimal.ZERO)
            .doubleValue());
  }

  @Test
  void shouldCalcularIVANetoWhenVentaConFacturaB() {
    Producto producto = new Producto();
    producto.setPrecioVentaPublico(new BigDecimal("1000"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(0,
        facturaServiceImpl.calcularIVANetoRenglon(
                Movimiento.VENTA, TipoDeComprobante.FACTURA_B, producto, BigDecimal.ZERO)
            .compareTo(new BigDecimal("210")));
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaA() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(0,
        facturaServiceImpl.calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_A, producto)
            .compareTo(new BigDecimal("121")));
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaVentaConFacturaX() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(0,
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
    assertEquals(0,
        facturaServiceImpl.calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_A, producto)
            .compareTo(new BigDecimal("100")));
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaX() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(0,
        facturaServiceImpl.calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_X, producto)
            .compareTo(new BigDecimal("100")));
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaB() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(0,
        facturaServiceImpl.calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_B, producto)
            .compareTo(new BigDecimal("121")));
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaC() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(0,
        facturaServiceImpl.calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_C, producto)
            .compareTo(new BigDecimal("121")));
  }

  @Test
  void shouldCalcularPrecioUnitarioWhenEsUnaCompraConFacturaY() {
    Producto producto = new Producto();
    producto.setPrecioCosto(new BigDecimal("100"));
    producto.setPrecioVentaPublico(new BigDecimal("121"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    assertEquals(0,
        facturaServiceImpl.calcularPrecioUnitario(Movimiento.COMPRA, TipoDeComprobante.FACTURA_Y, producto)
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
    assertEquals(0,
        facturaServiceImpl.calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_B, producto)
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
    assertEquals(0,
        facturaServiceImpl.calcularPrecioUnitario(Movimiento.VENTA, TipoDeComprobante.FACTURA_C, producto)
            .compareTo(new BigDecimal("242")));
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
  void shouldThrownBusinessServiceExceptionPorBusquedaVentaSinIdSucursal() {
    BusquedaFacturaVentaCriteria criteria = BusquedaFacturaVentaCriteria.builder().build();
    when(messageSourceTestMock.getMessage(
            "mensaje_busqueda_sin_sucursal", null, Locale.getDefault()))
        .thenReturn(
            messageSourceTest.getMessage(
                "mensaje_busqueda_sin_sucursal", null, Locale.getDefault()));
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> facturaVentaServiceImpl.getBuilderVenta(criteria, 1L));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSourceTest.getMessage(
                    "mensaje_busqueda_sin_sucursal", null, Locale.getDefault())));
  }

  @Test
  void shouldTestBusquedaFacturaVentaCriteria() {
    Usuario usuarioLogueado = new Usuario();
    List<Rol> roles = Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR);
    usuarioLogueado.setRoles(roles);
    when(mockUsuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder()
            .idSucursal(1L)
            .fechaDesde(LocalDateTime.MIN)
            .fechaHasta(LocalDateTime.MIN)
            .idCliente(1L)
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .idProducto(3L)
            .numSerie(4L)
            .numFactura(5L)
            .idUsuario(7L)
            .idViajante(9L)
            .nroPedido(33L)
            .build();
    String resultadoBuilder =
        "facturaVenta.sucursal.idSucursal = 1 && facturaVenta.eliminada = false "
            + "&& facturaVenta.fecha between -999999999-01-01T00:00 and -999999999-01-01T23:59:59.999999999 "
            + "&& facturaVenta.cliente.idCliente = 1 && facturaVenta.tipoComprobante = FACTURA_A "
            + "&& facturaVenta.usuario.idUsuario = 7 && facturaVenta.cliente.viajante.idUsuario = 9 "
            + "&& facturaVenta.numSerie = 4 && facturaVenta.numFactura = 5 && facturaVenta.pedido.nroPedido = 33 "
            + "&& any(facturaVenta.renglones).idProductoItem = 3";
    assertEquals(
        resultadoBuilder, facturaVentaServiceImpl.getBuilderVenta(criteria, 1L).toString());
    criteria =
        BusquedaFacturaVentaCriteria.builder()
            .idCliente(1L)
            .idSucursal(1L)
            .fechaDesde(LocalDateTime.MIN)
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .idProducto(3L)
            .numSerie(4L)
            .numFactura(5L)
            .idUsuario(7L)
            .idViajante(9L)
            .nroPedido(33L)
            .build();
    resultadoBuilder =
        "facturaVenta.sucursal.idSucursal = 1 && facturaVenta.eliminada = false "
            + "&& facturaVenta.fecha > -999999999-01-01T00:00 "
            + "&& facturaVenta.cliente.idCliente = 1 && facturaVenta.tipoComprobante = FACTURA_A "
            + "&& facturaVenta.usuario.idUsuario = 7 && facturaVenta.cliente.viajante.idUsuario = 9 "
            + "&& facturaVenta.numSerie = 4 && facturaVenta.numFactura = 5 && facturaVenta.pedido.nroPedido = 33 "
            + "&& any(facturaVenta.renglones).idProductoItem = 3";
    assertEquals(
        resultadoBuilder, facturaVentaServiceImpl.getBuilderVenta(criteria, 1L).toString());
    criteria =
        BusquedaFacturaVentaCriteria.builder()
            .idSucursal(1L)
            .fechaHasta(LocalDateTime.MIN)
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .idProducto(3L)
            .numSerie(4L)
            .numFactura(5L)
            .idUsuario(7L)
            .idViajante(9L)
            .idCliente(1L)
            .nroPedido(33L)
            .build();
    resultadoBuilder =
        "facturaVenta.sucursal.idSucursal = 1 && facturaVenta.eliminada = false "
            + "&& facturaVenta.fecha < -999999999-01-01T23:59:59.999999999 "
            + "&& facturaVenta.cliente.idCliente = 1 && facturaVenta.tipoComprobante = FACTURA_A "
            + "&& facturaVenta.usuario.idUsuario = 7 && facturaVenta.cliente.viajante.idUsuario = 9 "
            + "&& facturaVenta.numSerie = 4 && facturaVenta.numFactura = 5 && facturaVenta.pedido.nroPedido = 33 "
            + "&& any(facturaVenta.renglones).idProductoItem = 3";
    assertEquals(
        resultadoBuilder, facturaVentaServiceImpl.getBuilderVenta(criteria, 1L).toString());
    roles = Collections.singletonList(Rol.COMPRADOR);
    usuarioLogueado.setRoles(roles);
    when(mockUsuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    Cliente clienteRelacionadoConUsuarioLogueado =  new Cliente();
    clienteRelacionadoConUsuarioLogueado.setIdCliente(6L);
    when(mockClienteService.getClientePorIdUsuario(1L))
        .thenReturn(clienteRelacionadoConUsuarioLogueado);
    criteria =
        BusquedaFacturaVentaCriteria.builder()
            .idSucursal(1L)
            .fechaHasta(LocalDateTime.MIN)
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .idProducto(3L)
            .numSerie(4L)
            .numFactura(5L)
            .idUsuario(7L)
            .idViajante(9L)
            .idCliente(1L)
            .nroPedido(33L)
            .build();
    resultadoBuilder =
        "facturaVenta.sucursal.idSucursal = 1 && facturaVenta.eliminada = false " +
                "&& facturaVenta.fecha < -999999999-01-01T23:59:59.999999999 " +
                "&& facturaVenta.cliente.idCliente = 1 && facturaVenta.tipoComprobante = FACTURA_A " +
                "&& facturaVenta.usuario.idUsuario = 7 && facturaVenta.cliente.viajante.idUsuario = 9 " +
                "&& facturaVenta.numSerie = 4 && facturaVenta.numFactura = 5 && facturaVenta.pedido.nroPedido = 33 " +
                "&& any(facturaVenta.renglones).idProductoItem = 3 && facturaVenta.cliente.idCliente = 6";
    assertEquals(
        resultadoBuilder, facturaVentaServiceImpl.getBuilderVenta(criteria, 1L).toString());
    when(mockClienteService.getClientePorIdUsuario(1L))
            .thenReturn(null);
    criteria =
        BusquedaFacturaVentaCriteria.builder()
            .idSucursal(1L)
            .fechaHasta(LocalDateTime.MIN)
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .idProducto(3L)
            .numSerie(4L)
            .numFactura(5L)
            .idUsuario(7L)
            .idViajante(9L)
            .idCliente(1L)
            .nroPedido(33L)
            .build();
    resultadoBuilder =
        "facturaVenta.sucursal.idSucursal = 1 && facturaVenta.eliminada = false "
            + "&& facturaVenta.fecha < -999999999-01-01T23:59:59.999999999 "
            + "&& facturaVenta.cliente.idCliente = 1 && facturaVenta.tipoComprobante = FACTURA_A "
            + "&& facturaVenta.usuario.idUsuario = 7 && facturaVenta.cliente.viajante.idUsuario = 9 "
            + "&& facturaVenta.numSerie = 4 && facturaVenta.numFactura = 5 && facturaVenta.pedido.nroPedido = 33 "
            + "&& any(facturaVenta.renglones).idProductoItem = 3 && facturaVenta.cliente is null";
    assertEquals(
        resultadoBuilder, facturaVentaServiceImpl.getBuilderVenta(criteria, 1L).toString());
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
    when(mockFacturaCompraRepository.calcularTotalFacturadoCompra(builder))
            .thenReturn(null);
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

  @Test
  void shouldCalcularTotalFacturadoVenta() {
    Usuario usuarioLogueado = new Usuario();
    List<Rol> roles = Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR);
    usuarioLogueado.setRoles(roles);
    when(mockUsuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaVenta qFacturVenta = QFacturaVenta.facturaVenta;
    builder.and(
        qFacturVenta
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturVenta.eliminada.eq(false)));
    when(mockFacturaVentaRepository.calcularTotalFacturadoVenta(builder))
        .thenReturn(BigDecimal.TEN);
    assertEquals(BigDecimal.TEN, facturaVentaServiceImpl.calcularTotalFacturadoVenta(criteria, 1L));
  }

  @Test
  void shouldCalcularTotalFacturadoVentaAndReturnZero() {
    Usuario usuarioLogueado = new Usuario();
    List<Rol> roles = Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR);
    usuarioLogueado.setRoles(roles);
    when(mockUsuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaVenta qFacturVenta = QFacturaVenta.facturaVenta;
    builder.and(
        qFacturVenta
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturVenta.eliminada.eq(false)));
    when(mockFacturaVentaRepository.calcularTotalFacturadoVenta(builder)).thenReturn(null);
    assertEquals(
        BigDecimal.ZERO, facturaVentaServiceImpl.calcularTotalFacturadoVenta(criteria, 1L));
  }

  @Test
  void shouldCalcularIvaVenta() {
    Usuario usuarioLogueado = new Usuario();
    List<Rol> roles = Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR);
    usuarioLogueado.setRoles(roles);
    when(mockUsuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaVenta qFacturVenta = QFacturaVenta.facturaVenta;
    builder.and(
        qFacturVenta
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturVenta.eliminada.eq(false)));
    TipoDeComprobante[] tipoFactura = {TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_B};
    when(mockFacturaVentaRepository.calcularIVAVenta(builder, tipoFactura))
        .thenReturn(BigDecimal.TEN);
    assertEquals(BigDecimal.TEN, facturaVentaServiceImpl.calcularIvaVenta(criteria, 1L));
  }

  @Test
  void shouldCalcularIvaVentaAndReturnZero() {
    Usuario usuarioLogueado = new Usuario();
    List<Rol> roles = Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR);
    usuarioLogueado.setRoles(roles);
    when(mockUsuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaVenta qFacturVenta = QFacturaVenta.facturaVenta;
    builder.and(
        qFacturVenta
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturVenta.eliminada.eq(false)));
    TipoDeComprobante[] tipoFactura = {TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_B};
    when(mockFacturaVentaRepository.calcularIVAVenta(builder, tipoFactura)).thenReturn(null);
    assertEquals(BigDecimal.ZERO, facturaVentaServiceImpl.calcularIvaVenta(criteria, 1L));
  }

  @Test
  void shouldCalcularGananciaTotalVenta() {
    Usuario usuarioLogueado = new Usuario();
    List<Rol> roles = Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR);
    usuarioLogueado.setRoles(roles);
    when(mockUsuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaVenta qFacturVenta = QFacturaVenta.facturaVenta;
    builder.and(
        qFacturVenta
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturVenta.eliminada.eq(false)));
    when(mockFacturaVentaRepository.calcularGananciaTotal(builder)).thenReturn(BigDecimal.TEN);
    assertEquals(BigDecimal.TEN, facturaVentaServiceImpl.calcularGananciaTotal(criteria, 1L));
  }

  @Test
  void shouldCalcularGananciaTotalVentaAndReturnZero() {
    Usuario usuarioLogueado = new Usuario();
    List<Rol> roles = Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR);
    usuarioLogueado.setRoles(roles);
    when(mockUsuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaVenta qFacturVenta = QFacturaVenta.facturaVenta;
    builder.and(
        qFacturVenta
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturVenta.eliminada.eq(false)));
    when(mockFacturaVentaRepository.calcularGananciaTotal(builder)).thenReturn(null);
    assertEquals(BigDecimal.ZERO, facturaVentaServiceImpl.calcularGananciaTotal(criteria, 1L));
  }

  @Test
  void shouldTestRenglonesDelPedidoParaFacturar() {
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    RenglonPedido renglonPedido1 = new RenglonPedido();
    renglonPedido1.setIdProductoItem(1L);
    renglonPedido1.setCantidad(new BigDecimal("10"));
    RenglonPedido renglonPedido2 = new RenglonPedido();
    renglonPedido2.setIdProductoItem(2L);
    renglonPedido2.setCantidad(new BigDecimal("20"));
    renglonesPedido.add(renglonPedido1);
    renglonesPedido.add(renglonPedido2);
    Map<Long, BigDecimal> renglonesDeFacturas = new HashMap<>();
    renglonesDeFacturas.put(1L, new BigDecimal("5"));
    renglonesDeFacturas.put(2L, new BigDecimal("15"));
    RenglonFactura renglonFactura1 = new RenglonFactura();
    renglonFactura1.setIdProductoItem(1L);
    renglonFactura1.setCantidad(new BigDecimal("5"));
    RenglonFactura renglonFactura2 = new RenglonFactura();
    renglonFactura2.setIdProductoItem(2L);
    renglonFactura2.setCantidad(new BigDecimal("5"));
    when(pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglon(1L)).thenReturn(renglonesPedido);
    when(pedidoService.getRenglonesFacturadosDelPedido(1L)).thenReturn(renglonesDeFacturas);
    when(mockFacturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_A,
            Movimiento.VENTA,
            NuevoRenglonFacturaDTO.builder()
                .cantidad(new BigDecimal("5"))
                .idProducto(1L)
                .renglonMarcado(false)
                .build()))
        .thenReturn(renglonFactura1);
    when(mockFacturaService.calcularRenglon(
            TipoDeComprobante.FACTURA_A,
            Movimiento.VENTA,
            NuevoRenglonFacturaDTO.builder()
                .cantidad(new BigDecimal("5"))
                .idProducto(2L)
                .renglonMarcado(false)
                .build()))
        .thenReturn(renglonFactura2);
    when(mockFacturaService.marcarRenglonParaAplicarBonificacion(1L, new BigDecimal("5")))
        .thenReturn(false);
    when(mockFacturaService.marcarRenglonParaAplicarBonificacion(2L, new BigDecimal("5")))
        .thenReturn(false);
    assertFalse(
        facturaVentaServiceImpl
            .getRenglonesPedidoParaFacturar(1L, TipoDeComprobante.FACTURA_A)
            .isEmpty());
    when(pedidoService.getRenglonesFacturadosDelPedido(1L)).thenReturn(null);
    assertFalse(
        facturaVentaServiceImpl
            .getRenglonesPedidoParaFacturar(1L, TipoDeComprobante.FACTURA_A)
            .isEmpty());
  }

  @Test
  void shouldTestEnviarFacturaPorEmail() {
    FacturaVenta facturaVenta = new FacturaVenta();
    facturaVenta.setIdFactura(1L);
    facturaVenta.setNumSerie(2L);
    facturaVenta.setNumFactura(1L);
    facturaVenta.setTipoComprobante(TipoDeComprobante.FACTURA_A);
    Cliente clienteDeFactura = new Cliente();
    facturaVenta.setCliente(clienteDeFactura);
    when(mockFacturaService.getFacturaNoEliminadaPorId(1L)).thenReturn(facturaVenta);
    when(messageSourceTestMock.getMessage(
            "mensaje_correo_factura_sin_cae", null, Locale.getDefault()))
        .thenReturn(
            messageSourceTest.getMessage(
                "mensaje_correo_factura_sin_cae", null, Locale.getDefault()));
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> facturaVentaServiceImpl.enviarFacturaVentaPorEmail(1L));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSourceTest.getMessage(
                    "mensaje_correo_factura_sin_cae", null, Locale.getDefault())));

    facturaVenta.setCae(123L);
    when(mockFacturaService.getFacturaNoEliminadaPorId(1L)).thenReturn(facturaVenta);
    when(messageSourceTestMock.getMessage(
            "mensaje_correo_cliente_sin_email", null, Locale.getDefault()))
        .thenReturn(
            messageSourceTest.getMessage(
                "mensaje_correo_cliente_sin_email", null, Locale.getDefault()));
    thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> facturaVentaServiceImpl.enviarFacturaVentaPorEmail(1L));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSourceTest.getMessage(
                    "mensaje_correo_cliente_sin_email", null, Locale.getDefault())));
    clienteDeFactura.setEmail("correo@decliente.com");
    facturaVenta.setCliente(clienteDeFactura);
    Sucursal sucursal = new Sucursal();
    sucursal.setNombre("Sucursal de test");
    facturaVenta.setSucursal(sucursal);
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    configuracionSucursal.setUsarFacturaVentaPreImpresa(true);
    when(mockFacturaService.getFacturaNoEliminadaPorId(1L)).thenReturn(facturaVenta);
    when(mockConfiguracionSucursalService.getConfiguracionSucursal(sucursal))
        .thenReturn(configuracionSucursal);
    facturaVentaServiceImpl.enviarFacturaVentaPorEmail(1L);
    Pedido pedido = new Pedido();
    pedido.setTipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL);
    pedido.setSucursal(sucursal);
    UbicacionDTO ubicacionDTO = UbicacionDTO.builder().build();
    ubicacionDTO.setCalle("Calle 123");
    pedido.setDetalleEnvio(ubicacionDTO);
    facturaVenta.setPedido(pedido);
    when(mockFacturaService.getFacturaNoEliminadaPorId(1L)).thenReturn(facturaVenta);
    facturaVentaServiceImpl.enviarFacturaVentaPorEmail(1L);
  }
}
