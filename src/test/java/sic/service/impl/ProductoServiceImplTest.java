package sic.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import com.querydsl.core.BooleanBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaProductoCriteria;
import sic.modelo.dto.NuevoProductoDTO;
import sic.modelo.dto.ProductoFaltanteDTO;
import sic.modelo.dto.ProductosParaActualizarDTO;
import sic.modelo.dto.ProductosParaVerificarStockDTO;
import sic.modelo.embeddable.CantidadProductoEmbeddable;
import sic.modelo.embeddable.PrecioProductoEmbeddable;
import sic.repository.ProductoFavoritoRepository;
import sic.repository.ProductoRepository;
import sic.util.CustomValidator;

import javax.persistence.EntityNotFoundException;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {ProductoServiceImpl.class, CustomValidator.class, MessageSource.class})
class ProductoServiceImplTest {

  @MockBean MedidaServiceImpl medidaService;
  @MockBean RubroServiceImpl rubroService;
  @MockBean ProveedorServiceImpl proveedorService;
  @MockBean SucursalServiceImpl sucursalService;
  @MockBean TraspasoServiceImpl traspasoService;
  @MockBean PedidoServiceImpl pedidoService;
  @MockBean ClienteServiceImpl clienteService;
  @MockBean CorreoElectronicoServiceImpl correoElectronicoService;
  @MockBean ProductoRepository productoRepository;
  @MockBean ProductoFavoritoRepository productoFavoritoRepository;
  @MockBean MessageSource messageSource;

  @Autowired ProductoServiceImpl productoService;

  private Producto construirProducto() {
    Producto producto = new Producto();
    producto.setIdProducto(1L);
    producto.setCodigo("1");
    producto.setDescripcion("Cinta adhesiva doble faz 3M");
    producto.setMedida(new Medida());
    producto.setPrecioProducto(new PrecioProductoEmbeddable());
    producto.getPrecioProducto().setPrecioCosto(new BigDecimal("89.35"));
    producto.getPrecioProducto().setGananciaPorcentaje(new BigDecimal("38.74"));
    producto.getPrecioProducto().setGananciaNeto(new BigDecimal("34.614"));
    producto.getPrecioProducto().setPrecioVentaPublico(new BigDecimal("123.964"));
    producto.getPrecioProducto().setIvaPorcentaje(new BigDecimal("21"));
    producto.getPrecioProducto().setIvaNeto(new BigDecimal("26.032"));
    producto.getPrecioProducto().setPrecioLista(new BigDecimal("150"));
    producto.getPrecioProducto().setPorcentajeBonificacionPrecio(new BigDecimal("10"));
    producto.getPrecioProducto().setPrecioBonificado(new BigDecimal("135"));
    producto.getPrecioProducto().setPorcentajeBonificacionOferta(BigDecimal.ZERO);
    producto.setCantidadProducto(new CantidadProductoEmbeddable());
    producto.getCantidadProducto().setBulto(new BigDecimal("5"));
    producto.setFechaAlta(LocalDateTime.now());
    producto.setFechaUltimaModificacion(LocalDateTime.now());
    Sucursal sucursal = new Sucursal();
    Rubro rubro = new Rubro();
    Proveedor proveedor = new Proveedor();
    Set<CantidadEnSucursal> cantidadEnSucursales = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursal = new CantidadEnSucursal();
    cantidadEnSucursal.setCantidad(BigDecimal.TEN);
    cantidadEnSucursal.setSucursal(sucursal);
    cantidadEnSucursales.add(cantidadEnSucursal);
    cantidadEnSucursal.setSucursal(sucursal);
    producto.getCantidadProducto().setCantidadEnSucursales(cantidadEnSucursales);
    producto.getCantidadProducto().setCantidadTotalEnSucursales(BigDecimal.TEN);
    producto.setRubro(rubro);
    producto.setProveedor(proveedor);
    return producto;
  }

  @Test
  void shouldCalcularGananciaPorcentajeDescendente() {
    BigDecimal precioCosto = new BigDecimal("12.34");
    BigDecimal pvp = new BigDecimal("23.45");
    BigDecimal resultadoEsperado = new BigDecimal("90.032414910859000");
    BigDecimal resultadoObtenido =
        productoService.calcularGananciaPorcentaje(null, null, pvp, null, null, precioCosto, false);
    assertEquals(resultadoEsperado, resultadoObtenido);
  }

  @Test
  void shouldCalcularGananciaPorcentajeAscendente() {
    BigDecimal precioCosto = new BigDecimal("12.34");
    BigDecimal pvp = new BigDecimal("23.45");
    BigDecimal resultadoEsperado = new BigDecimal("95.223662884927066");
    BigDecimal resultadoObtenido =
        productoService.calcularGananciaPorcentaje(
            new BigDecimal("30"),
            new BigDecimal("25"),
            pvp,
            new BigDecimal("21"),
            BigDecimal.ZERO,
            precioCosto,
            true);
    assertEquals(resultadoEsperado, resultadoObtenido);
  }

  @Test
  void shouldCalcularGananciaNeto() {
    BigDecimal precioCosto = new BigDecimal("12.34");
    BigDecimal gananciaPorcentaje = new BigDecimal("100");
    BigDecimal resultadoEsperado = new BigDecimal("12.340000000000000");
    BigDecimal resultadoObtenido =
        productoService.calcularGananciaNeto(precioCosto, gananciaPorcentaje);
    assertEquals(resultadoEsperado, resultadoObtenido);
  }

  @Test
  void shouldcalcularCalcularPVP() {
    BigDecimal precioCosto = new BigDecimal("12.34");
    BigDecimal gananciaPorcentaje = new BigDecimal("100");
    BigDecimal resultadoEsperado = new BigDecimal("24.68000000000000000");
    BigDecimal resultadoObtenido = productoService.calcularPVP(precioCosto, gananciaPorcentaje);
    assertEquals(resultadoEsperado, resultadoObtenido);
  }

  @Test
  void shouldCalcularIVANeto() {
    BigDecimal pvp = new BigDecimal("24.68");
    BigDecimal ivaPorcentaje = new BigDecimal("21");
    BigDecimal resultadoEsperado = new BigDecimal("5.182800000000000");
    BigDecimal resultadoObtenido = productoService.calcularIVANeto(pvp, ivaPorcentaje);
    assertEquals(resultadoEsperado, resultadoObtenido);
  }

  @Test
  void shouldCalcularPrecioLista() {
    BigDecimal pvp = new BigDecimal("24.68");
    BigDecimal ivaPorcentaje = new BigDecimal("21");
    BigDecimal resultadoEsperado = new BigDecimal("29.86280000000000000");
    BigDecimal resultadoObtenido = productoService.calcularPrecioLista(pvp, ivaPorcentaje);
    assertEquals(resultadoEsperado, resultadoObtenido);
  }

  @Test
  void shouldThrownBusinessExceptionAltaProductoCodigoDuplicado() {
    NuevoProductoDTO nuevoProductoUno =
        NuevoProductoDTO.builder()
            .descripcion("Ventilador de pie")
            .cantidadEnSucursal(
                new HashMap<Long, BigDecimal>() {
                  {
                    put(1L, BigDecimal.TEN);
                  }
                })
            .bulto(BigDecimal.ONE)
            .precioCosto(new BigDecimal("100"))
            .gananciaPorcentaje(new BigDecimal("900"))
            .gananciaNeto(new BigDecimal("900"))
            .precioVentaPublico(new BigDecimal("1000"))
            .ivaPorcentaje(new BigDecimal("21.0"))
            .ivaNeto(new BigDecimal("210"))
            .precioLista(new BigDecimal("1210"))
            .porcentajeBonificacionPrecio(new BigDecimal("20"))
            .porcentajeBonificacionOferta(new BigDecimal("-1"))
            .oferta(true)
            .imagen((new String("imagen")).getBytes())
            .codigo("12345")
            .build();
    when(proveedorService.getProveedorNoEliminadoPorId(1L)).thenReturn(new Proveedor());
    when(rubroService.getRubroNoEliminadoPorId(1L)).thenReturn(new Rubro());
    when(medidaService.getMedidaNoEliminadaPorId(1L)).thenReturn(new Medida());
    Sucursal[] sucursales = new Sucursal[] {new Sucursal()};
    when(sucursalService.getSucusales(false)).thenReturn(Arrays.asList(sucursales));
    when(productoRepository.findByDescripcionAndEliminado("Ventilador de pie", false))
        .thenReturn(null);
    Producto productoDuplicado = new Producto();
    productoDuplicado.setDescripcion("12345");
    when(productoRepository.findByCodigoAndEliminado("12345", false)).thenReturn(productoDuplicado);
    assertThrows(
        BusinessServiceException.class,
        () -> productoService.guardar(nuevoProductoUno, 1L, 1L, 1L));
    verify(messageSource).getMessage(eq("mensaje_producto_duplicado_codigo"), any(), any());
  }

  @Test
  void shouldThrownBusinessExceptionAltaProductoDescripcionDuplicado() {
    NuevoProductoDTO nuevoProductoUno =
        NuevoProductoDTO.builder()
            .descripcion("Ventilador de pie")
            .cantidadEnSucursal(
                new HashMap<Long, BigDecimal>() {
                  {
                    put(1L, BigDecimal.TEN);
                  }
                })
            .bulto(BigDecimal.ONE)
            .precioCosto(new BigDecimal("100"))
            .gananciaPorcentaje(new BigDecimal("900"))
            .gananciaNeto(new BigDecimal("900"))
            .precioVentaPublico(new BigDecimal("1000"))
            .ivaPorcentaje(new BigDecimal("21.0"))
            .ivaNeto(new BigDecimal("210"))
            .precioLista(new BigDecimal("1210"))
            .porcentajeBonificacionPrecio(new BigDecimal("20"))
            .porcentajeBonificacionOferta(new BigDecimal("-1"))
            .oferta(true)
            .imagen((new String("imagen")).getBytes())
            .codigo("12345")
            .build();
    when(proveedorService.getProveedorNoEliminadoPorId(1L)).thenReturn(new Proveedor());
    when(rubroService.getRubroNoEliminadoPorId(1L)).thenReturn(new Rubro());
    when(medidaService.getMedidaNoEliminadaPorId(1L)).thenReturn(new Medida());
    Sucursal[] sucursales = new Sucursal[] {new Sucursal()};
    when(sucursalService.getSucusales(false)).thenReturn(Arrays.asList(sucursales));
    Producto productoDuplicado = new Producto();
    productoDuplicado.setDescripcion("Ventilador de pie");
    when(productoRepository.findByDescripcionAndEliminado("Ventilador de pie", false))
        .thenReturn(productoDuplicado);
    assertThrows(
        BusinessServiceException.class,
        () -> productoService.guardar(nuevoProductoUno, 1L, 1L, 1L));
    verify(messageSource).getMessage(eq("mensaje_producto_duplicado_descripcion"), any(), any());
  }

  @Test
  void shouldThrownBusinessExceptionActualizacionProductoDuplicadoCodigo() {
    Producto productoParaActualizar = this.construirProducto();
    productoParaActualizar.setIdProducto(1L);
    Producto productoPersistido = this.construirProducto();
    productoPersistido.setIdProducto(2L);
    when(productoRepository.findByDescripcionAndEliminado("Cinta adhesiva doble faz 3M", false))
        .thenReturn(productoPersistido);
    assertThrows(
        BusinessServiceException.class,
        () -> productoService.actualizar(productoParaActualizar, productoPersistido, null));
    verify(messageSource).getMessage(eq("mensaje_producto_duplicado_descripcion"), any(), any());
  }

  @Test
  void shouldTestBusquedaCriteria() {
    BusquedaProductoCriteria criteria =
        BusquedaProductoCriteria.builder()
            .codigo("213")
            .descripcion("testDescripcion uno")
            .idRubro(1L)
            .idProveedor(2L)
            .listarSoloEnStock(true)
            .listarSoloEnStock(true)
            .publico(true)
            .oferta(true)
            .build();
    String stringBuilder =
        "producto.eliminado = false && (containsIc(producto.codigo,213) || containsIc(producto.descripcion,testDescripcion) "
            + "&& containsIc(producto.descripcion,uno)) "
            + "&& producto.rubro.idRubro = 1 && producto.proveedor.idProveedor = 2 "
            + "&& any(producto.cantidadProducto.cantidadEnSucursales).cantidad > 0 "
            + "&& producto.cantidadProducto.ilimitado = false "
            + "&& producto.publico = true && producto.precioProducto.oferta = true";
    assertEquals(stringBuilder, productoService.getBuilder(criteria).toString());
  }

  @Test
  void shouldGetProductoConPrecioBonificadoPorOferta() {
    Producto producto = new Producto();
    producto.setPrecioProducto(new PrecioProductoEmbeddable());
    producto.getPrecioProducto().setPrecioLista(new BigDecimal("100"));
    producto.getPrecioProducto().setPorcentajeBonificacionOferta(new BigDecimal("10"));
    producto.getPrecioProducto().setOferta(true);
    producto.getPrecioProducto().setPrecioBonificado(new BigDecimal("90.00"));
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
    Producto productoRecuperado = productoService.getProductoNoEliminadoPorId(1L);
    assertEquals(new BigDecimal("100"), productoRecuperado.getPrecioProducto().getPrecioLista());
    assertEquals(new BigDecimal("90.00"), productoRecuperado.getPrecioProducto().getPrecioBonificado());
  }

  @Test
  void shouldThrownEntityNotFoundException() {
    when(productoRepository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(
        EntityNotFoundException.class, () -> productoService.getProductoNoEliminadoPorId(1L));
    verify(messageSource).getMessage(eq("mensaje_producto_no_existente"), any(), any());
  }

  @Test
  void shouldDevolverNullSiCodigoVacio() {
    assertNull(productoService.getProductoPorCodigo("123"));
  }

  @Test
  void shouldGetProductosSinStockDisponible() {
    Producto producto = new Producto();
    producto.setIdProducto(1L);
    producto.setCantidadProducto(new CantidadProductoEmbeddable());
    producto.getCantidadProducto().setCantidadTotalEnSucursales(new BigDecimal("9"));
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    sucursal.setConfiguracionSucursal(configuracionSucursal);
    CantidadEnSucursal cantidadEnSucursal = new CantidadEnSucursal();
    cantidadEnSucursal.setSucursal(sucursal);
    cantidadEnSucursal.setCantidad(new BigDecimal("9"));
    Set<CantidadEnSucursal> cantidadEnSucursales = new HashSet<>();
    cantidadEnSucursales.add(cantidadEnSucursal);
    producto.getCantidadProducto().setCantidadEnSucursales(cantidadEnSucursales);
    producto.getCantidadProducto().setIlimitado(false);
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
    long[] idProducto = {1};
    BigDecimal[] cantidad = {BigDecimal.TEN.add(BigDecimal.ONE)};
    assertThrows(
        BusinessServiceException.class,
        () ->
            productoService.getProductosSinStockDisponible(
                ProductosParaVerificarStockDTO.builder()
                    .cantidad(cantidad)
                    .idProducto(idProducto)
                    .build()));
    verify(messageSource).getMessage(eq("mensaje_producto_consulta_stock_sin_sucursal"), any(), any());
    List<ProductoFaltanteDTO> resultadoObtenido =
        productoService.getProductosSinStockDisponible(ProductosParaVerificarStockDTO.builder()
                .cantidad(cantidad)
                .idProducto(idProducto)
                .idSucursal(1L)
                .build());
    Assertions.assertFalse(resultadoObtenido.isEmpty());
    assertEquals(new BigDecimal("11"), resultadoObtenido.get(0).getCantidadSolicitada());
    assertEquals(new BigDecimal("9"), resultadoObtenido.get(0).getCantidadDisponible());
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setIdProductoItem(1L);
    renglonPedido.setCantidad(BigDecimal.ONE);
    renglonesPedido.add(renglonPedido);
    when(pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglon(1L)).thenReturn(renglonesPedido);
    resultadoObtenido =
        productoService.getProductosSinStockDisponible(ProductosParaVerificarStockDTO.builder()
                .cantidad(cantidad)
                .idProducto(idProducto)
                .idSucursal(1L)
                .idPedido(1L)
                .build());
    Assertions.assertFalse(resultadoObtenido.isEmpty());
    assertEquals(new BigDecimal("10"), resultadoObtenido.get(0).getCantidadSolicitada());
    assertEquals(new BigDecimal("9"), resultadoObtenido.get(0).getCantidadDisponible());
  }

  @Test
  void shouldTestActualizarMultiplesProductos() {
    Producto producto = this.construirProducto();
    producto.setIdProducto(1L);
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
    Usuario usuario = new Usuario();
    usuario.setRoles(Collections.emptyList());
    assertThrows(
        BusinessServiceException.class,
        () ->
            productoService.actualizarMultiples(
                ProductosParaActualizarDTO.builder()
                    .idProducto(new long[] {1L})
                    .descuentoRecargoPorcentaje(BigDecimal.TEN)
                    .cantidadVentaMinima(BigDecimal.TEN)
                    .idMedida(1L)
                    .idRubro(1L)
                    .idProveedor(2L)
                    .gananciaPorcentaje(BigDecimal.TEN)
                    .ivaPorcentaje(new BigDecimal("21"))
                    .precioCosto(BigDecimal.TEN)
                    .porcentajeBonificacionPrecio(BigDecimal.TEN)
                    .publico(true)
                    .build(), usuario));
    verify(messageSource).getMessage(eq("mensaje_modificar_producto_no_permitido"), any(), any());
    assertThrows(
        BusinessServiceException.class,
        () ->
            productoService.actualizarMultiples(
                ProductosParaActualizarDTO.builder()
                    .idProducto(new long[] {1L, 1L})
                    .cantidadVentaMinima(BigDecimal.TEN)
                    .idMedida(1L)
                    .idRubro(1L)
                    .idProveedor(2L)
                    .gananciaPorcentaje(BigDecimal.TEN)
                    .ivaPorcentaje(new BigDecimal("21"))
                    .precioCosto(BigDecimal.TEN)
                    .porcentajeBonificacionPrecio(BigDecimal.TEN)
                    .publico(true)
                    .build(), usuario));
    verify(messageSource).getMessage(eq("mensaje_error_ids_duplicados"), any(), any());
    Producto producto1 = new Producto();
    producto1.setIdProducto(1L);
    producto1.setCodigo("1a");
    producto1.setCantidadProducto(new CantidadProductoEmbeddable());
    producto1.setPrecioProducto(new PrecioProductoEmbeddable());
    Producto producto2 = new Producto();
    producto2.setIdProducto(2L);
    producto2.setCodigo("2b");
    producto2.setCantidadProducto(new CantidadProductoEmbeddable());
    producto2.setPrecioProducto(new PrecioProductoEmbeddable());
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
    when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
    productoService.actualizarMultiples(
            ProductosParaActualizarDTO.builder()
                    .idProducto(new long[] {1L, 2L})
                    .cantidadVentaMinima(BigDecimal.TEN)
                    .idMedida(1L)
                    .idRubro(1L)
                    .idProveedor(2L)
                    .gananciaPorcentaje(BigDecimal.TEN)
                    .ivaPorcentaje(new BigDecimal("21"))
                    .precioCosto(BigDecimal.TEN)
                    .porcentajeBonificacionPrecio(new BigDecimal("5"))
                    .porcentajeBonificacionOferta(BigDecimal.TEN)
                    .publico(true)
                    .build(), usuario);
    productoService.actualizarMultiples(
            ProductosParaActualizarDTO.builder()
                    .idProducto(new long[] {1L, 2L})
                    .cantidadVentaMinima(BigDecimal.TEN)
                    .idMedida(1L)
                    .idRubro(1L)
                    .idProveedor(2L)
                    .gananciaPorcentaje(BigDecimal.TEN)
                    .ivaPorcentaje(new BigDecimal("21"))
                    .precioCosto(BigDecimal.TEN)
                    .porcentajeBonificacionPrecio(new BigDecimal("5"))
                    .porcentajeBonificacionOferta(null)
                    .publico(true)
                    .build(), usuario);
    verify(productoRepository, times(2)).saveAll(any());
  }

  @Test
  void shouldValidarReglasDeNegocio() {
    Producto producto = this.construirProducto();
    producto.setIdProducto(1L);
    producto.getPrecioProducto().setOferta(true);
    producto.getPrecioProducto().setPorcentajeBonificacionOferta(new BigDecimal("-1"));
    assertThrows(
        BusinessServiceException.class,
        () -> productoService.validarReglasDeNegocio(TipoDeOperacion.ALTA, producto));
    verify(messageSource).getMessage(eq("mensaje_producto_oferta_inferior_0"), any(), any());
    producto.getPrecioProducto().setPorcentajeBonificacionOferta(new BigDecimal("10"));
    Producto productoDuplicado = this.construirProducto();
    productoDuplicado.setIdProducto(2L);
    when(productoRepository.findByCodigoAndEliminado(producto.getCodigo(), false))
        .thenReturn(productoDuplicado);
    assertThrows(
        BusinessServiceException.class,
        () -> productoService.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, producto));
    verify(messageSource).getMessage(eq("mensaje_producto_duplicado_codigo"), any(), any());
  }

  @Test
  void shouldValidarCalculos() {
    Producto producto = this.construirProducto();
    producto.getPrecioProducto().setIvaPorcentaje(BigDecimal.TEN);
    assertThrows(BusinessServiceException.class, () -> productoService.validarCalculos(producto));
    verify(messageSource).getMessage(eq("mensaje_error_iva_no_valido"), any(), any());
    producto.getPrecioProducto().setIvaPorcentaje(new BigDecimal("21.0"));
    BigDecimal valorAuxiliar = producto.getPrecioProducto().getGananciaNeto();
    producto.getPrecioProducto().setGananciaNeto(BigDecimal.ONE);
    assertThrows(BusinessServiceException.class, () -> productoService.validarCalculos(producto));
    verify(messageSource).getMessage(eq("mensaje_producto_ganancia_neta_incorrecta"), any(), any());
    producto.getPrecioProducto().setGananciaNeto(valorAuxiliar);
    valorAuxiliar = producto.getPrecioProducto().getIvaNeto();
    producto.getPrecioProducto().setIvaNeto(BigDecimal.ONE);
    assertThrows(BusinessServiceException.class, () -> productoService.validarCalculos(producto));
    verify(messageSource).getMessage(eq("mensaje_producto_iva_neto_incorrecto"), any(), any());
    producto.getPrecioProducto().setIvaNeto(valorAuxiliar);
    producto.getPrecioProducto().setPrecioLista(BigDecimal.ONE);
    assertThrows(BusinessServiceException.class, () -> productoService.validarCalculos(producto));
    verify(messageSource).getMessage(eq("mensaje_producto_precio_lista_incorrecto"), any(), any());
  }

  @Test
  void shouldTestReporteListaDePrecios() {
    List<Producto> productos = new ArrayList<>();
    Producto productoParaReporte = new Producto();
    productos.add(productoParaReporte);
    when(productoRepository.findAll(
            productoService.getBuilder(BusquedaProductoCriteria.builder().build()),
            productoService.getPageable(null, null, null, Integer.MAX_VALUE)))
        .thenReturn(new PageImpl<>(productos));
    Sucursal sucursal = new Sucursal();
    sucursal.setLogo("noTieneImagen");
    sucursal.setEmail("correo@gmail.com");
    when(sucursalService.getSucursalPredeterminada()).thenReturn(sucursal);
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    assertThrows(
        ServiceException.class,
        () ->
            productoService.getListaDePreciosEnPdf(BusquedaProductoCriteria.builder().build(), 1L));
    assertThrows(
        ServiceException.class,
        () ->
            productoService.getListaDePreciosEnXls(BusquedaProductoCriteria.builder().build(), 1L));
    verify(messageSource, times(2)).getMessage(eq("mensaje_recurso_no_encontrado"), any(), any());
    sucursal.setLogo(null);
    BusquedaProductoCriteria criteria = BusquedaProductoCriteria.builder().build();
    productoService.getListaDePreciosEnPdf(criteria, 1L);
    productoService.getListaDePreciosEnXls(criteria, 1L);
    verify(correoElectronicoService, times(2))
        .enviarEmail(
            eq("correo@gmail.com"), eq(""), eq("Listado de productos"), eq(""), any(), any());
  }

  @Test
  void shouldGetPageableProducto() {
    Pageable pageable = productoService.getPageable(0, null, null, Integer.MAX_VALUE);
    assertEquals("descripcion: ASC", pageable.getSort().toString());
    assertEquals(0, pageable.getPageNumber());
    pageable = productoService.getPageable(1, "rubro.nombre", "ASC", Integer.MAX_VALUE);
    assertEquals("rubro.nombre: ASC", pageable.getSort().toString());
    assertEquals(1, pageable.getPageNumber());
    pageable = productoService.getPageable(3, "rubro.nombre", "DESC", Integer.MAX_VALUE);
    assertEquals("rubro.nombre: DESC", pageable.getSort().toString());
    assertEquals(3, pageable.getPageNumber());
    pageable = productoService.getPageable(3, "codigo", "NO", Integer.MAX_VALUE);
    assertEquals("descripcion: DESC", pageable.getSort().toString());
    assertEquals(3, pageable.getPageNumber());
    pageable = productoService.getPageable(null, "codigo", "NO", Integer.MAX_VALUE);
    assertEquals("descripcion: DESC", pageable.getSort().toString());
    assertEquals(0, pageable.getPageNumber());
  }

  @Test
  void shouldActualizarStockTraspaso() {
    Producto producto1 = new Producto();
    producto1.setIdProducto(1L);
    producto1.setCantidadProducto(new CantidadProductoEmbeddable());
    producto1.getCantidadProducto().setCantidadTotalEnSucursales(new BigDecimal("5"));
    producto1.setDescripcion("Ventilador de pie");
    Sucursal sucursalOrigen = new Sucursal();
    sucursalOrigen.setIdSucursal(1L);
    sucursalOrigen.setNombre("Sucursal Uno");
    ConfiguracionSucursal configuracionSucursalOrigen = new ConfiguracionSucursal();
    configuracionSucursalOrigen.setComparteStock(true);
    sucursalOrigen.setConfiguracionSucursal(configuracionSucursalOrigen);
    Sucursal sucursalDestino = new Sucursal();
    sucursalDestino.setIdSucursal(2L);
    sucursalDestino.setNombre("Sucursal dos");
    ConfiguracionSucursal configuracionSucursalDestino = new ConfiguracionSucursal();
    configuracionSucursalDestino.setComparteStock(false);
    sucursalDestino.setConfiguracionSucursal(configuracionSucursalOrigen);
    CantidadEnSucursal cantidadEnSucursalProducto1 = new CantidadEnSucursal();
    cantidadEnSucursalProducto1.setSucursal(sucursalOrigen);
    cantidadEnSucursalProducto1.setCantidad(new BigDecimal("3"));
    CantidadEnSucursal cantidadEnSucursal2Producto1 = new CantidadEnSucursal();
    cantidadEnSucursal2Producto1.setSucursal(sucursalDestino);
    cantidadEnSucursal2Producto1.setCantidad(new BigDecimal("2"));
    Set<CantidadEnSucursal> cantidadEnSucursalesProducto1 = new HashSet<>();
    cantidadEnSucursalesProducto1.add(cantidadEnSucursalProducto1);
    cantidadEnSucursalesProducto1.add(cantidadEnSucursal2Producto1);
    producto1.getCantidadProducto().setCantidadEnSucursales(cantidadEnSucursalesProducto1);
    producto1.getCantidadProducto().setIlimitado(false);
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
    when(productoRepository.save(producto1)).thenReturn(producto1);
    Producto producto2 = new Producto();
    producto2.setIdProducto(2L);
    producto2.setCantidadProducto(new CantidadProductoEmbeddable());
    producto2.getCantidadProducto().setCantidadTotalEnSucursales(new BigDecimal("2"));
    producto2.setDescripcion("Duchas");
    CantidadEnSucursal cantidadEnSucursalProducto2 = new CantidadEnSucursal();
    cantidadEnSucursalProducto2.setSucursal(sucursalOrigen);
    cantidadEnSucursalProducto2.setCantidad(new BigDecimal("2"));
    Set<CantidadEnSucursal> cantidadEnSucursalesProducto2 = new HashSet<>();
    cantidadEnSucursalesProducto2.add(cantidadEnSucursalProducto2);
    producto2.getCantidadProducto().setCantidadEnSucursales(cantidadEnSucursalesProducto2);
    producto2.getCantidadProducto().setIlimitado(false);
    when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
    when(productoRepository.save(producto2)).thenReturn(producto2);
    Traspaso traspaso = new Traspaso();
    List<RenglonTraspaso> renglones = new ArrayList<>();
    RenglonTraspaso renglonTraspaso1 = new RenglonTraspaso();
    renglonTraspaso1.setIdProducto(1L);
    renglonTraspaso1.setCantidadProducto(BigDecimal.TEN);
    RenglonTraspaso renglonTraspaso2 = new RenglonTraspaso();
    renglonTraspaso2.setIdProducto(2L);
    renglonTraspaso2.setCantidadProducto(BigDecimal.ONE);
    renglones.add(renglonTraspaso1);
    renglones.add(renglonTraspaso2);
    traspaso.setRenglones(renglones);
    traspaso.setSucursalOrigen(sucursalOrigen);
    traspaso.setSucursalDestino(sucursalDestino);
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursalOrigen);
    when(sucursalService.getSucursalPorId(2L)).thenReturn(sucursalDestino);
    assertThrows(
        BusinessServiceException.class,
        () -> productoService.actualizarStockTraspaso(traspaso, TipoDeOperacion.ALTA));
    verify(messageSource).getMessage(eq("mensaje_traspaso_sin_stock"), any(), any());
    producto1.getCantidadProducto().setCantidadTotalEnSucursales(new BigDecimal("20"));
    producto1
        .getCantidadProducto().getCantidadEnSucursales()
        .forEach(cantidadEnSucursal -> cantidadEnSucursal.setCantidad(BigDecimal.TEN));
    productoService.actualizarStockTraspaso(traspaso, TipoDeOperacion.ALTA);
    productoService.actualizarStockTraspaso(traspaso, TipoDeOperacion.ELIMINACION);
    verify(productoRepository, times(8)).save(any());
    assertThrows(
        BusinessServiceException.class,
        () -> productoService.actualizarStockTraspaso(traspaso, TipoDeOperacion.ACTUALIZACION));
    verify(messageSource).getMessage(eq("mensaje_operacion_no_soportada"), any(), any());
  }

  @Test
  void shouldDevolverStockPedido() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.ABIERTO);
    List<RenglonPedido> renglones = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setIdProductoItem(1L);
    renglones.add(renglonPedido);
    pedido.setRenglones(renglones);
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    pedido.setSucursal(sucursal);
    List<RenglonPedido> renglonesAnteriores = new ArrayList<>();
    RenglonPedido renglonPedidoAnterior = new RenglonPedido();
    renglonPedidoAnterior.setIdProductoItem(1L);
    renglonesAnteriores.add(renglonPedido);
    productoService.devolverStockPedido(
        pedido, TipoDeOperacion.ACTUALIZACION, renglonesAnteriores, 1L);
    verify(messageSource)
        .getMessage(eq("mensaje_error_actualizar_stock_producto_eliminado"), any(), any());
    Producto producto = this.construirProducto();
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
    when(productoRepository.save(producto)).thenReturn(producto);
    productoService.devolverStockPedido(
        pedido, TipoDeOperacion.ACTUALIZACION, renglonesAnteriores, 1L);
    verify(messageSource).getMessage(eq("mensaje_producto_agrega_stock"), any(), any());
    verify(productoRepository).save(producto);
  }

  @Test
  void shouldActualizarStockPedido() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.ABIERTO);
    List<RenglonPedido> renglones = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setIdProductoItem(1L);
    renglones.add(renglonPedido);
    pedido.setRenglones(renglones);
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    pedido.setSucursal(sucursal);
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ALTA);
    verify(messageSource)
        .getMessage(eq("mensaje_error_actualizar_stock_producto_eliminado"), any(), any());
    Producto producto = this.construirProducto();
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
    when(productoRepository.save(producto)).thenReturn(producto);
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ALTA);
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ACTUALIZACION);
    verify(messageSource, times(2)).getMessage(eq("mensaje_producto_quita_stock"), any(), any());
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ELIMINACION);
    pedido.setEstado(EstadoPedido.CANCELADO);
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ACTUALIZACION);
    verify(messageSource, times(2)).getMessage(eq("mensaje_producto_agrega_stock"), any(), any());
    verify(traspasoService, times(3)).guardarTraspasosPorPedido(pedido);
    verify(traspasoService, times(3)).eliminarTraspasoDePedido(pedido);
  }

  @Test
  void shouldActualizarStockNotaCredito() {
    Map<Long, BigDecimal> idsYCantidades = new HashMap<>();
    idsYCantidades.put(1L, new BigDecimal("10"));
    idsYCantidades.put(2L, new BigDecimal("20"));
    Sucursal sucursal = new Sucursal();
    sucursal.setNombre("primera sucursal");
    Producto producto1 = new Producto();
    producto1.setIdProducto(1L);
    Set<CantidadEnSucursal> cantidadesEnSucursalProducto1 = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursalProducto1 = new CantidadEnSucursal();
    cantidadEnSucursalProducto1.setSucursal(sucursal);
    cantidadEnSucursalProducto1.setCantidad(BigDecimal.ONE);
    cantidadesEnSucursalProducto1.add(cantidadEnSucursalProducto1);
    producto1.setCantidadProducto(new CantidadProductoEmbeddable());
    producto1.getCantidadProducto().setCantidadEnSucursales(cantidadesEnSucursalProducto1);
    producto1.getCantidadProducto().setCantidadTotalEnSucursales(BigDecimal.ONE);
    Producto producto2 = new Producto();
    producto2.setIdProducto(2L);
    Set<CantidadEnSucursal> cantidadesEnSucursalProducto2 = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursalProducto2 = new CantidadEnSucursal();
    cantidadEnSucursalProducto2.setSucursal(sucursal);
    cantidadEnSucursalProducto2.setCantidad(BigDecimal.ONE);
    cantidadesEnSucursalProducto2.add(cantidadEnSucursalProducto2);
    producto2.setCantidadProducto(new CantidadProductoEmbeddable());
    producto2.getCantidadProducto().setCantidadEnSucursales(cantidadesEnSucursalProducto2);
    producto2.getCantidadProducto().setCantidadTotalEnSucursales(BigDecimal.ONE);
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
    when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
    productoService.actualizarStockNotaCredito(
        idsYCantidades, 1L, TipoDeOperacion.ALTA, Movimiento.VENTA);
    productoService.actualizarStockNotaCredito(
        idsYCantidades, 1L, TipoDeOperacion.ALTA, Movimiento.COMPRA);
    productoService.actualizarStockNotaCredito(
        idsYCantidades, 1L, TipoDeOperacion.ELIMINACION, Movimiento.VENTA);
    productoService.actualizarStockNotaCredito(
        idsYCantidades, 1L, TipoDeOperacion.ELIMINACION, Movimiento.COMPRA);
    verify(messageSource, times(4))
        .getMessage(eq("mensaje_producto_agrega_stock"), any(), eq(Locale.getDefault()));
    verify(messageSource, times(4))
        .getMessage(eq("mensaje_producto_quita_stock"), any(), eq(Locale.getDefault()));
    verify(productoRepository, times(8)).save(any());
    assertThrows(
        BusinessServiceException.class,
        () ->
            productoService.actualizarStockNotaCredito(
                idsYCantidades, 1L, TipoDeOperacion.ACTUALIZACION, Movimiento.VENTA));
    verify(messageSource)
        .getMessage(eq("mensaje_operacion_no_soportada"), any(), eq(Locale.getDefault()));
    assertThrows(
        BusinessServiceException.class,
        () ->
            productoService.actualizarStockNotaCredito(
                idsYCantidades, 1L, TipoDeOperacion.ALTA, Movimiento.PEDIDO));
    assertThrows(
        BusinessServiceException.class,
        () ->
            productoService.actualizarStockNotaCredito(
                idsYCantidades, 1L, TipoDeOperacion.ELIMINACION, Movimiento.PEDIDO));
    verify(messageSource, times(2))
        .getMessage(
            eq("mensaje_preference_tipo_de_movimiento_no_soportado"),
            any(),
            eq(Locale.getDefault()));
    idsYCantidades.clear();
    idsYCantidades.put(3L, BigDecimal.ONE);
    productoService.actualizarStockNotaCredito(
        idsYCantidades, 1L, TipoDeOperacion.ALTA, Movimiento.VENTA);
    verify(messageSource)
        .getMessage(
            eq("mensaje_error_actualizar_stock_producto_eliminado"),
            any(),
            eq(Locale.getDefault()));
  }

  @Test
  void shouldCalcularCantidadEnSucursalesDisponible() {
    Sucursal sucursalUno = new Sucursal();
    sucursalUno.setNombre("primera sucursal");
    sucursalUno.setIdSucursal(1L);
    Sucursal sucursalDos = new Sucursal();
    sucursalDos.setNombre("segunda sucursal");
    sucursalDos.setIdSucursal(2L);
    Sucursal sucursalTres = new Sucursal();
    sucursalTres.setNombre("segunda tres");
    sucursalTres.setIdSucursal(3L);
    ConfiguracionSucursal configuracionSucursalUno = new ConfiguracionSucursal();
    configuracionSucursalUno.setIdConfiguracionSucursal(1L);
    ConfiguracionSucursal configuracionSucursalDos = new ConfiguracionSucursal();
    configuracionSucursalDos.setIdConfiguracionSucursal(2L);
    ConfiguracionSucursal configuracionSucursalTres = new ConfiguracionSucursal();
    configuracionSucursalTres.setComparteStock(true);
    configuracionSucursalTres.setIdConfiguracionSucursal(3L);
    sucursalUno.setConfiguracionSucursal(configuracionSucursalUno);
    sucursalDos.setConfiguracionSucursal(configuracionSucursalDos);
    sucursalTres.setConfiguracionSucursal(configuracionSucursalTres);
    Producto producto1 = new Producto();
    producto1.setIdProducto(1L);
    Set<CantidadEnSucursal> cantidadesEnSucursalProducto = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursalProducto1 = new CantidadEnSucursal();
    cantidadEnSucursalProducto1.setSucursal(sucursalUno);
    cantidadEnSucursalProducto1.setCantidad(BigDecimal.ONE);
    cantidadesEnSucursalProducto.add(cantidadEnSucursalProducto1);
    CantidadEnSucursal cantidadEnSucursalProducto2 = new CantidadEnSucursal();
    cantidadEnSucursalProducto2.setSucursal(sucursalDos);
    cantidadEnSucursalProducto2.setCantidad(new BigDecimal("20"));
    cantidadesEnSucursalProducto.add(cantidadEnSucursalProducto2);
    CantidadEnSucursal cantidadEnSucursalProducto3 = new CantidadEnSucursal();
    cantidadEnSucursalProducto3.setSucursal(sucursalTres);
    cantidadEnSucursalProducto3.setCantidad(BigDecimal.TEN);
    cantidadesEnSucursalProducto.add(cantidadEnSucursalProducto3);
    producto1.setCantidadProducto(new CantidadProductoEmbeddable());
    producto1.getCantidadProducto().setCantidadEnSucursales(cantidadesEnSucursalProducto);
    Producto productoRecuperado =
        productoService.calcularCantidadEnSucursalesDisponible(producto1, 1L);
    assertNotNull(productoRecuperado);
    List<CantidadEnSucursal> listaCantidadEnSucursales =
        new ArrayList<>(productoRecuperado.getCantidadProducto().getCantidadEnSucursalesDisponible());
    assertEquals(2, listaCantidadEnSucursales.size());
    assertEquals(BigDecimal.ONE, listaCantidadEnSucursales.get(0).getCantidad());
    assertEquals(BigDecimal.TEN, listaCantidadEnSucursales.get(1).getCantidad());
  }

  @Test
  void shouldTestGuardarProductoFavorito() {
    Producto producto = new Producto();
    producto.setIdProducto(1L);
    producto.setDescripcion("producto uno");
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
    Cliente cliente = new Cliente();
    cliente.setIdCliente(1L);
    cliente.setNombreFiscal("San Wuchito");
    when(clienteService.getClientePorIdUsuario(1L)).thenReturn(cliente);
    ProductoFavorito productoFavorito = new ProductoFavorito();
    productoFavorito.setCliente(cliente);
    productoFavorito.setProducto(producto);
    when(productoFavoritoRepository.save(productoFavorito)).thenReturn(productoFavorito);
    productoService.guardarProductoFavorito(1L, 1L);
    verify(productoFavoritoRepository).save(productoFavorito);
    verify(messageSource).getMessage(eq("mensaje_producto_favorito_agregado"), eq(new Object[] {producto}), any());
  }

  @Test
  void shouldTestGetPaginaProductosFavoritosDelCliente() {
    Cliente cliente = new Cliente();
    cliente.setIdCliente(1L);
    cliente.setNombreFiscal("San Wuchito");
    when(clienteService.getClientePorIdUsuario(1L)).thenReturn(cliente);
    Producto producto = new Producto();
    producto.setDescripcion("Producto Test");
    producto.setIdProducto(1L);
    CantidadProductoEmbeddable cantidadProductoEmbeddable = new CantidadProductoEmbeddable();
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    Set<CantidadEnSucursal> cantidadesEnSucursal = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursal = new CantidadEnSucursal();
    cantidadEnSucursal.setIdCantidadEnSucursal(1L);
    cantidadEnSucursal.setCantidad(BigDecimal.TEN);
    cantidadEnSucursal.setSucursal(sucursal);
    cantidadProductoEmbeddable.setCantidadEnSucursales(cantidadesEnSucursal);
    producto.setCantidadProducto(cantidadProductoEmbeddable);
    List<ProductoFavorito> productosFavoritos = new ArrayList<>();
    ProductoFavorito productoFavorito = new ProductoFavorito();
    productoFavorito.setCliente(cliente);
    productoFavorito.setProducto(producto);
    productosFavoritos.add(productoFavorito);
    Page<ProductoFavorito> pageable = new PageImpl<>(productosFavoritos, PageRequest.of(0, 1, Sort.by("idProductoFavorito")), 0);
    when(productoFavoritoRepository.findAll(
            any(), eq(PageRequest.of(1, 15, Sort.by(Sort.Direction.DESC, "idProductoFavorito")))))
        .thenReturn(pageable);
    Page<Producto> paginaProductos = productoService.getPaginaProductosFavoritosDelCliente(1L, 1L, 1);
    assertNotNull(paginaProductos);
    assertEquals(paginaProductos.getTotalElements(), pageable.getTotalElements());
    assertEquals(paginaProductos.getTotalPages(), pageable.getTotalElements());
    assertEquals(paginaProductos.getNumber(), pageable.getNumber());
    assertTrue(paginaProductos.getContent().get(0).isFavorito());
  }

  @Test
  void shouldTestGetProductosFavoritosDelCliente() {
    Cliente cliente = new Cliente();
    cliente.setIdCliente(1L);
    cliente.setNombreFiscal("San Wuchito");
    when(clienteService.getClientePorIdUsuario(1L)).thenReturn(cliente);
    List<ProductoFavorito> paginaProductosFavoritos = new ArrayList<>();
    Producto productoUno = new Producto();
    productoUno.setDescripcion("Producto Test");
    productoUno.setIdProducto(1L);
    Producto productoDos = new Producto();
    productoDos.setDescripcion("Producto Test");
    productoDos.setIdProducto(1L);
    ProductoFavorito productoFavoritoUno = new ProductoFavorito();
    productoFavoritoUno.setCliente(cliente);
    productoFavoritoUno.setProducto(productoUno);
    paginaProductosFavoritos.add(productoFavoritoUno);
    ProductoFavorito productoFavoritoDos = new ProductoFavorito();
    productoFavoritoDos.setCliente(cliente);
    productoFavoritoDos.setProducto(productoDos);
    paginaProductosFavoritos.add(productoFavoritoDos);
    when(productoFavoritoRepository.findAllByCliente(cliente)).thenReturn(paginaProductosFavoritos);
    List<Producto> productosFavoritos = productoService.getProductosFavoritosDelClientePorIdUsuario(1L);
    assertNotNull(productosFavoritos);
    assertTrue(productosFavoritos.get(0).isFavorito());
    assertTrue(productosFavoritos.get(1).isFavorito());
  }

  @Test
  void shouldTestQuitarProductoDeFavoritos() {
    Cliente cliente = new Cliente();
    cliente.setIdCliente(1L);
    cliente.setNombreFiscal("San Wuchito");
    when(clienteService.getClientePorIdUsuario(1L)).thenReturn(cliente);
    Producto producto = new Producto();
    producto.setDescripcion("Producto Test");
    producto.setIdProducto(1L);
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
    productoService.quitarProductoDeFavoritos(1L, 1L);
    verify(productoFavoritoRepository).deleteByClienteAndProducto(cliente, producto);
    verify(messageSource)
        .getMessage(eq("mensaje_producto_favorito_quitado"), eq(new Object[] {producto}), eq(Locale.getDefault()));
  }

  @Test
  void shouldTestBuscarProductos() {
    Cliente cliente = new Cliente();
    cliente.setIdCliente(1L);
    cliente.setNombreFiscal("San Wuchito");
    when(clienteService.getClientePorIdUsuario(1L)).thenReturn(cliente);
    List<Producto> productos = new ArrayList<>();
    Producto productoUno = new Producto();
    productoUno.setIdProducto(1L);
    productoUno.setDescripcion("Producto Uno");
    Producto productoDos = new Producto();
    productoDos.setIdProducto(2L);
    productoDos.setDescripcion("Producto Dos");
    productos.add(productoUno);
    productos.add(productoDos);
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    sucursal.setConfiguracionSucursal(configuracionSucursal);
    Set<CantidadEnSucursal> cantidadEnSucursales = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursal = new CantidadEnSucursal();
    cantidadEnSucursal.setCantidad(BigDecimal.TEN);
    cantidadEnSucursal.setSucursal(sucursal);
    cantidadEnSucursales.add(cantidadEnSucursal);
    cantidadEnSucursal.setSucursal(sucursal);
    productoUno.setCantidadProducto(new CantidadProductoEmbeddable());
    productoUno.getCantidadProducto().setCantidadEnSucursales(cantidadEnSucursales);
    productoUno.getCantidadProducto().setCantidadTotalEnSucursales(BigDecimal.TEN);
    productoDos.setCantidadProducto(new CantidadProductoEmbeddable());
    productoDos.getCantidadProducto().setCantidadEnSucursales(cantidadEnSucursales);
    productoDos.getCantidadProducto().setCantidadTotalEnSucursales(BigDecimal.TEN);
    Page<Producto> paginaProductos = new PageImpl<>(productos);
    BusquedaProductoCriteria criteriaProductos =
        BusquedaProductoCriteria.builder().pagina(0).ordenarPor("descripcion").sentido("ASC").build();
    BooleanBuilder builder = productoService.getBuilder(criteriaProductos);
    Pageable pageable =
        productoService.getPageable(
            criteriaProductos.getPagina(),
            criteriaProductos.getOrdenarPor(),
            criteriaProductos.getSentido(),
            15);
    when(productoRepository.findAll(builder, pageable)).thenReturn(paginaProductos);
    productoService.buscarProductos(criteriaProductos, 1L);
    verify(productoRepository).findAll(eq(builder), eq(pageable));
  }

  @Test
  void shouldTestQuitarTodosLosProductosDeFavoritosDelCliente() {
    Cliente cliente = new Cliente();
    cliente.setIdCliente(1L);
    cliente.setNombreFiscal("San Wuchito");
    when(clienteService.getClientePorIdUsuario(1L)).thenReturn(cliente);
    productoService.quitarProductosDeFavoritos(1L);
    verify(productoFavoritoRepository).deleteAllByCliente(cliente);
    verify(messageSource)
            .getMessage(eq("mensaje_producto_favoritos_quitados"), eq(null), eq(Locale.getDefault()));
  }

  @Test
  void shouldTestIsFavorito() {
    Cliente cliente = new Cliente();
    cliente.setIdCliente(1L);
    cliente.setNombreFiscal("San Wuchito");
    when(clienteService.getClientePorIdUsuario(1L)).thenReturn(cliente);
    Producto productoUno = new Producto();
    productoUno.setIdProducto(1L);
    productoUno.setDescripcion("Producto Uno");
    when(productoRepository.findById(1L)).thenReturn(Optional.of(productoUno));
    when(productoFavoritoRepository.existsByClienteAndProducto(cliente, productoUno))
        .thenReturn(true);
    assertTrue(productoService.isFavorito(1L, 1L));
    verify(productoFavoritoRepository).existsByClienteAndProducto(cliente, productoUno);
  }

  @Test
  void shouldGetCantidadDeProductoFavorito() {
    Cliente cliente = new Cliente();
    cliente.setNombreFiscal("Cliente test");
    when(clienteService.getClientePorIdUsuario(1L)).thenReturn(cliente);
    productoService.getCantidadDeProductosFavoritos(1L);
    verify(productoFavoritoRepository).getCantidadDeArticulosEnFavoritos(cliente);
  }

  @Test
  void shouldValidarLongitudDeArrays() {
    int longitudIds = 4;
    int longitudCantidades = 3;
    assertThrows(
            BusinessServiceException.class,
            () -> productoService.validarLongitudDeArrays(longitudIds, longitudCantidades));
    verify(messageSource).getMessage("mensaje_error_logitudes_arrays", null, Locale.getDefault());
  }

  @Test
  void shouldConstruirNuevoProductoFaltante() {
    Producto producto = new Producto();
    producto.setIdProducto(1L);
    producto.setCodigo("321");
    producto.setDescripcion("Producto test");
    Sucursal sucursal = new Sucursal();
    sucursal.setNombre("Sucursal Test");
    sucursal.setIdSucursal(2L);
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    ProductoFaltanteDTO productoFaltante = productoService.construirNuevoProductoFaltante(producto, BigDecimal.TEN, BigDecimal.ONE, 1L);
    assertNotNull(productoFaltante);
    assertEquals(1L, productoFaltante.getIdProducto());
    assertEquals("321", productoFaltante.getCodigo());
    assertEquals("Producto test", productoFaltante.getDescripcion());
    assertEquals("Sucursal Test", productoFaltante.getNombreSucursal());
    assertEquals(2L, productoFaltante.getIdSucursal());
    assertEquals(BigDecimal.TEN, productoFaltante.getCantidadSolicitada());
    assertEquals(BigDecimal.ONE, productoFaltante.getCantidadDisponible());
  }

  @Test
  void shouldGetProductosRelacionados() {
    Producto producto = new Producto();
    producto.setIdProducto(1L);
    producto.setCodigo("321");
    producto.setDescripcion("Producto test");
    Rubro rubro = new Rubro();
    rubro.setIdRubro(1L);
    rubro.setNombre("Ferretería");
    producto.setRubro(rubro);
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    sucursal.setConfiguracionSucursal(configuracionSucursal);
    producto.setCantidadProducto(new CantidadProductoEmbeddable());
    CantidadEnSucursal cantidadEnSucursal = new CantidadEnSucursal();
    cantidadEnSucursal.setSucursal(sucursal);
    cantidadEnSucursal.setCantidad(new BigDecimal("9"));
    Set<CantidadEnSucursal> cantidadEnSucursales = new HashSet<>();
    cantidadEnSucursales.add(cantidadEnSucursal);
    producto.getCantidadProducto().setCantidadEnSucursales(cantidadEnSucursales);
    producto.getCantidadProducto().setIlimitado(false);
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
    List<Producto> listaProducto = new ArrayList<>();
    listaProducto.add(producto);
    Page<Producto> newPage = new PageImpl<>(listaProducto);
    Pageable pageable = PageRequest.of(0, 15);
    when(productoRepository.buscarProductosRelacionadosPorRubro(1L, 1L, pageable)).thenReturn(newPage);
    assertEquals(newPage, productoService.getProductosRelacionados(1L, 1L, 0));
    }

  @Test
  void shouldAgregarCantidadReservada() {
    productoService.agregarCantidadReservada(1L, BigDecimal.TEN);
    verify(productoRepository).actualizarCantidadReservada(1L, BigDecimal.TEN);
  }

  @Test
  void shouldQuitarCantidadReservada() {
    productoService.quitarCantidadReservada(1L, BigDecimal.TEN);
    verify(productoRepository).actualizarCantidadReservada(1L, BigDecimal.TEN.negate());
  }
}
