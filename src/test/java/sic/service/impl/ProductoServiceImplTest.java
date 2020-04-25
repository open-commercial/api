package sic.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaProductoCriteria;
import sic.modelo.dto.NuevoProductoDTO;
import sic.modelo.dto.ProductoFaltanteDTO;
import sic.modelo.dto.ProductosParaActualizarDTO;
import sic.modelo.dto.ProductosParaVerificarStockDTO;

import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class ProductoServiceImplTest {

  @Autowired MessageSource messageSource;
  @Autowired ProductoServiceImpl productoService;
  @Autowired ProveedorServiceImpl proveedorService;
  @Autowired MedidaServiceImpl medidaService;
  @Autowired RubroServiceImpl rubroService;
  @Autowired SucursalServiceImpl sucursalService;

  private NuevoProductoDTO crearNuevoProductoDTO(Sucursal sucursal) {
    return NuevoProductoDTO.builder()
        .descripcion("Corta Papas - Vegetales")
        .codigo("XD3.M2")
        .cantidadEnSucursal(
            new HashMap<Long, BigDecimal>() {
              {
                put(sucursal.getIdSucursal(), BigDecimal.TEN);
              }
            })
        .bulto(BigDecimal.ONE)
        .precioCosto(new BigDecimal("100"))
        .gananciaPorcentaje(new BigDecimal("900"))
        .gananciaNeto(new BigDecimal("900"))
        .precioVentaPublico(new BigDecimal("1000"))
        .ivaPorcentaje(new BigDecimal("10.5"))
        .ivaNeto(new BigDecimal("105"))
        .precioLista(new BigDecimal("1105"))
        .porcentajeBonificacionPrecio(new BigDecimal("20"))
        .publico(true)
        .build();
  }

  private Proveedor crearProveedor() {
    Proveedor proveedor = new Proveedor();
    proveedor.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    byte[] array = new byte[7];
    new Random().nextBytes(array);
    String generatedString = new String(array, StandardCharsets.UTF_8);
    proveedor.setNroProveedor(generatedString);
    proveedor.setRazonSocial(generatedString);
    return proveedor;
  }

  private Medida crearMedida() {
    Medida medida = new Medida();
    byte[] array = new byte[7];
    new Random().nextBytes(array);
    String generatedString = new String(array, StandardCharsets.UTF_8);
    medida.setNombre(generatedString);
    return medida;
  }

  private Rubro crearRubro() {
    Rubro rubro = new Rubro();
    byte[] array = new byte[7];
    new Random().nextBytes(array);
    String generatedString = new String(array, StandardCharsets.UTF_8);
    rubro.setNombre(generatedString);
    return rubro;
  }

  private Sucursal crearSucursal() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    byte[] array = new byte[7];
    new Random().nextBytes(array);
    String generatedString = new String(array, StandardCharsets.UTF_8);
    sucursal.setNombre(generatedString);
    int leftLimit = 97; // letter 'a'
    int rightLimit = 122; // letter 'z'
    int targetStringLength = 10;
    Random random = new Random();
    generatedString =
        random
            .ints(leftLimit, rightLimit + 1)
            .limit(targetStringLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    sucursal.setEmail(generatedString + "@" + generatedString + ".com");
    long leftLimitLong = 1L;
    long rightLimitLong = 1000000L;
    long generatedLong = leftLimitLong + (long) (Math.random() * (rightLimitLong - leftLimitLong));
    sucursal.setIdFiscal(generatedLong);
    return sucursal;
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
  public void shouldThrownBusinessExceptionAltaProductoCodigoDuplicado() {
    Proveedor proveedor = proveedorService.guardar(this.crearProveedor());
    Medida medida = medidaService.guardar(this.crearMedida());
    Rubro rubro = rubroService.guardar(this.crearRubro());
    Sucursal sucursal = sucursalService.guardar(this.crearSucursal());
    NuevoProductoDTO nuevoProductoDTO = this.crearNuevoProductoDTO(sucursal);
    nuevoProductoDTO.setCodigo("RES.8");
    nuevoProductoDTO.setDescripcion("RES.8");
    assertNotNull(
        productoService.guardar(
            nuevoProductoDTO,
            medida.getIdMedida(),
            rubro.getIdRubro(),
            proveedor.getIdProveedor()));
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () ->
                productoService.guardar(
                    nuevoProductoDTO,
                    medida.getIdMedida(),
                    rubro.getIdRubro(),
                    proveedor.getIdProveedor()));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_producto_duplicado_codigo", null, Locale.getDefault())));
  }

  @Test
  public void shouldThrownBusinessExceptionAltaProductoDescripcionDuplicado() {
    Proveedor proveedor = proveedorService.guardar(this.crearProveedor());
    Medida medida = medidaService.guardar(this.crearMedida());
    Rubro rubro = rubroService.guardar(this.crearRubro());
    Sucursal sucursal = sucursalService.guardar(this.crearSucursal());
    NuevoProductoDTO nuevoProducto = this.crearNuevoProductoDTO(sucursal);
    nuevoProducto.setCodigo(null);
    nuevoProducto.setDescripcion("Ventilador de pared");
    assertNotNull(
        productoService.guardar(
            nuevoProducto, medida.getIdMedida(), rubro.getIdRubro(), proveedor.getIdProveedor()));
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () ->
                productoService.guardar(
                    nuevoProducto,
                    medida.getIdMedida(),
                    rubro.getIdRubro(),
                    proveedor.getIdProveedor()));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_producto_duplicado_descripcion", null, Locale.getDefault())));
  }

  @Test
  public void shouldThrownBusinessExceptionActualizarProductoSinImagen() {
    Proveedor proveedor = proveedorService.guardar(this.crearProveedor());
    Medida medida = medidaService.guardar(this.crearMedida());
    Rubro rubro = rubroService.guardar(this.crearRubro());
    Sucursal sucursal = sucursalService.guardar(this.crearSucursal());
    Producto producto =
        productoService.guardar(
            this.crearNuevoProductoDTO(sucursal),
            medida.getIdMedida(),
            rubro.getIdRubro(),
            proveedor.getIdProveedor());
    producto.setOferta(true);
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> productoService.actualizar(producto, producto, null));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_producto_oferta_sin_imagen",
                    new Object[] {producto.getDescripcion()},
                    Locale.getDefault())));
  }

  @Test
  public void shouldThrownBusinessExceptionActualizacionProductoDuplicadoCodigo() {
    Proveedor proveedor = proveedorService.guardar(this.crearProveedor());
    Medida medida = medidaService.guardar(this.crearMedida());
    Rubro rubro = rubroService.guardar(this.crearRubro());
    Sucursal sucursal = sucursalService.guardar(this.crearSucursal());
    NuevoProductoDTO nuevoProductoDTO = this.crearNuevoProductoDTO(sucursal);
    nuevoProductoDTO.setCodigo("X9M.19");
    nuevoProductoDTO.setDescripcion("X9M.19");
    productoService.guardar(
        nuevoProductoDTO, medida.getIdMedida(), rubro.getIdRubro(), proveedor.getIdProveedor());
    nuevoProductoDTO.setCodigo("TLC.16");
    nuevoProductoDTO.setDescripcion("TLC.16");
    Producto productoParaActualizar =
        productoService.guardar(
            nuevoProductoDTO, medida.getIdMedida(), rubro.getIdRubro(), proveedor.getIdProveedor());
    productoParaActualizar.setCodigo("X9M.19");
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> productoService.actualizar(productoParaActualizar, productoParaActualizar, null));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_producto_duplicado_codigo",
                    new Object[] {productoParaActualizar.getDescripcion()},
                    Locale.getDefault())));
  }

  @Test
  public void shouldTestBusquedaCriteria() {
    BusquedaProductoCriteria criteria =
        BusquedaProductoCriteria.builder()
            .codigo("213")
            .descripcion("testDescripcion")
            .idRubro(1L)
            .idProveedor(2L)
            .listarSoloEnStock(true)
            .listarSoloEnStock(true)
            .publico(true)
            .oferta(true)
            .build();
    String stringBuilder =
        "producto.eliminado = false && (containsIc(producto.codigo,213) || "
            + "containsIc(producto.descripcion,testDescripcion)) "
            + "&& producto.rubro.idRubro = 1 && producto.proveedor.idProveedor = 2 "
            + "&& any(producto.cantidadEnSucursales).cantidad > 0 && producto.ilimitado = false "
            + "&& producto.publico = true "
            + "&& producto.oferta = true";
    assertEquals(stringBuilder, productoService.getBuilder(criteria).toString());
  }

  @Test
  public void shouldGetProductoConPrecioBonificadoPorOferta() throws IOException {
    Proveedor proveedor = proveedorService.guardar(this.crearProveedor());
    Medida medida = medidaService.guardar(this.crearMedida());
    Rubro rubro = rubroService.guardar(this.crearRubro());
    Sucursal sucursal = sucursalService.guardar(this.crearSucursal());
    NuevoProductoDTO nuevoProductoDTO = this.crearNuevoProductoDTO(sucursal);
    nuevoProductoDTO.setCodigo("T900M.19");
    nuevoProductoDTO.setDescripcion("Taladro 900w");
    nuevoProductoDTO.setPorcentajeBonificacionOferta(new BigDecimal("10"));
    nuevoProductoDTO.setOferta(true);
    BufferedImage bImage = ImageIO.read(getClass().getResource("/imagenProductoTest.jpeg"));
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ImageIO.write(bImage, "jpeg", bos);
    nuevoProductoDTO.setImagen(bos.toByteArray());
    Producto producto =
        productoService.guardar(
            nuevoProductoDTO, medida.getIdMedida(), rubro.getIdRubro(), proveedor.getIdProveedor());
    assertEquals(new BigDecimal("1105"), producto.getPrecioLista());
    assertEquals(new BigDecimal("994.500000000000000"), producto.getPrecioBonificado());
  }

  @Test
  public void shouldThrownEntityNotFoundException() {
    EntityNotFoundException thrown =
        assertThrows(
            EntityNotFoundException.class, () -> productoService.getProductoNoEliminadoPorId(999L));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_producto_no_existente", null, Locale.getDefault())));
  }

  @Test
  public void shouldDevolverNullSiCodigoVacio() {
    assertNull(productoService.getProductoPorCodigo("123"));
  }

  @Test
  @Transactional
  void shouldGetProductosSinStockDisponible() {
    Proveedor proveedor = proveedorService.guardar(this.crearProveedor());
    Medida medida = medidaService.guardar(this.crearMedida());
    Rubro rubro = rubroService.guardar(this.crearRubro());
    Sucursal sucursal = sucursalService.guardar(this.crearSucursal());
    NuevoProductoDTO nuevoProductoDTO = this.crearNuevoProductoDTO(sucursal);
    nuevoProductoDTO.setCodigo("TIM.38");
    nuevoProductoDTO.setDescripcion("TIM.38");
    Producto producto =
        productoService.guardar(
            nuevoProductoDTO, medida.getIdMedida(), rubro.getIdRubro(), proveedor.getIdProveedor());
    long[] idProducto = {producto.getIdProducto()};
    BigDecimal[] cantidad = {BigDecimal.TEN.add(BigDecimal.ONE)};
    ProductosParaVerificarStockDTO productosParaVerificarStockDTO =
        ProductosParaVerificarStockDTO.builder().build();
    productosParaVerificarStockDTO.setIdSucursal(sucursal.getIdSucursal());
    productosParaVerificarStockDTO.setCantidad(cantidad);
    productosParaVerificarStockDTO.setIdProducto(idProducto);
    List<ProductoFaltanteDTO> resultadoObtenido =
        productoService.getProductosSinStockDisponible(productosParaVerificarStockDTO);
    Assertions.assertFalse(resultadoObtenido.isEmpty());
  }

  @Test
  void shouldThrownBusinessServiceExceptionPorBuscarProductosSinIdSucursal() {
    long[] idProducto = {1};
    BigDecimal[] cantidad = {BigDecimal.TEN.add(BigDecimal.ONE)};
    ProductosParaVerificarStockDTO productosParaVerificarStockDTO =
        ProductosParaVerificarStockDTO.builder().cantidad(cantidad).idProducto(idProducto).build();
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> productoService.getProductosSinStockDisponible(productosParaVerificarStockDTO));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_consulta_stock_sin_sucursal", null, Locale.getDefault())));
  }

  @Test
  void shouldTestActualizarMultiplesProductos() {
    Proveedor proveedor = proveedorService.guardar(this.crearProveedor());
    Medida medida = medidaService.guardar(this.crearMedida());
    Rubro rubro = rubroService.guardar(this.crearRubro());
    Sucursal sucursal = sucursalService.guardar(this.crearSucursal());
    NuevoProductoDTO nuevoProductoDTO = this.crearNuevoProductoDTO(sucursal);
    nuevoProductoDTO.setCodigo("POM.98");
    nuevoProductoDTO.setDescripcion("POM.98");
    Producto producto =
        productoService.guardar(
            nuevoProductoDTO, medida.getIdMedida(), rubro.getIdRubro(), proveedor.getIdProveedor());
    ProductosParaActualizarDTO productosParaActualizarDTO =
        ProductosParaActualizarDTO.builder()
            .idProducto(new long[] {producto.getIdProducto()})
            .cantidadVentaMinima(BigDecimal.TEN)
            .idMedida(medida.getIdMedida())
            .idRubro(rubro.getIdRubro())
            .idProveedor(proveedor.getIdProveedor())
            .gananciaPorcentaje(BigDecimal.TEN)
            .ivaPorcentaje(new BigDecimal("21"))
            .precioCosto(BigDecimal.TEN)
            .porcentajeBonificacionPrecio(BigDecimal.TEN)
            .publico(true)
            .build();
    productoService.actualizarMultiples(productosParaActualizarDTO);
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () ->
                productoService.actualizarMultiples(
                    ProductosParaActualizarDTO.builder()
                        .idProducto(new long[] {producto.getIdProducto()})
                        .descuentoRecargoPorcentaje(BigDecimal.TEN)
                        .cantidadVentaMinima(BigDecimal.TEN)
                        .idMedida(medida.getIdMedida())
                        .idRubro(rubro.getIdRubro())
                        .idProveedor(proveedor.getIdProveedor())
                        .gananciaPorcentaje(BigDecimal.TEN)
                        .ivaPorcentaje(new BigDecimal("21"))
                        .precioCosto(BigDecimal.TEN)
                        .porcentajeBonificacionPrecio(BigDecimal.TEN)
                        .publico(true)
                        .build()));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_modificar_producto_no_permitido", null, Locale.getDefault())));
    thrown =
        assertThrows(
            BusinessServiceException.class,
            () ->
                productoService.actualizarMultiples(
                    ProductosParaActualizarDTO.builder()
                        .idProducto(new long[] {producto.getIdProducto(), producto.getIdProducto()})
                        .cantidadVentaMinima(BigDecimal.TEN)
                        .idMedida(medida.getIdMedida())
                        .idRubro(rubro.getIdRubro())
                        .idProveedor(proveedor.getIdProveedor())
                        .gananciaPorcentaje(BigDecimal.TEN)
                        .ivaPorcentaje(new BigDecimal("21"))
                        .precioCosto(BigDecimal.TEN)
                        .porcentajeBonificacionPrecio(BigDecimal.TEN)
                        .publico(true)
                        .build()));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_error_ids_duplicados", null, Locale.getDefault())));
  }

  @Test
  @Transactional
  void shouldTestActualizarStockPedido() {
    Proveedor proveedor = proveedorService.guardar(this.crearProveedor());
    Medida medida = medidaService.guardar(this.crearMedida());
    Rubro rubro = rubroService.guardar(this.crearRubro());
    Sucursal sucursal = sucursalService.guardar(this.crearSucursal());
    NuevoProductoDTO nuevoProductoDTO = this.crearNuevoProductoDTO(sucursal);
    nuevoProductoDTO.setCodigo(null);
    nuevoProductoDTO.setDescripcion("Corta Papas - Vegetales");
    Producto producto =
            productoService.guardar(
                    nuevoProductoDTO, medida.getIdMedida(), rubro.getIdRubro(), proveedor.getIdProveedor());
    assertEquals(BigDecimal.TEN, producto.getCantidadTotalEnSucursales());
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setIdProductoItem(1L);
    renglonPedido.setCantidad(new BigDecimal("2"));
    renglonesPedido.add(renglonPedido);
    Pedido pedido = new Pedido();
    pedido.setSucursal(sucursal);
    pedido.setRenglones(renglonesPedido);
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ALTA);
    producto = productoService.getProductoNoEliminadoPorId(1L);
    assertEquals(new BigDecimal("8"), producto.getCantidadTotalEnSucursales());
    pedido.setEstado(EstadoPedido.ABIERTO);
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ACTUALIZACION);
    assertEquals(new BigDecimal("6"), producto.getCantidadTotalEnSucursales());
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ELIMINACION);
    assertEquals(new BigDecimal("8"), producto.getCantidadTotalEnSucursales());
    pedido.setEstado(EstadoPedido.CERRADO);
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ACTUALIZACION);
    assertEquals(new BigDecimal("10"), producto.getCantidadTotalEnSucursales());
    producto.setEliminado(true);
    productoService.actualizar(producto, producto, null);
    assertTrue(producto.isEliminado());
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ACTUALIZACION);
  }
}
