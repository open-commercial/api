package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaFacturaVentaCriteria;
import sic.modelo.criteria.BusquedaTraspasoCriteria;
import sic.modelo.dto.NuevoTraspasoDTO;
import sic.modelo.dto.ProductoFaltanteDTO;
import sic.repository.RenglonTraspasoRepository;
import sic.repository.TraspasoRepository;
import sic.util.CustomValidator;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {CustomValidator.class, TraspasoServiceImpl.class, MessageSource.class})
public class TraspasoServiceImplTest {

  @MockBean ProductoServiceImpl productoService;
  @MockBean SucursalServiceImpl sucursalService;
  @MockBean UsuarioServiceImpl usuarioService;
  @MockBean TraspasoRepository traspasoRepository;
  @MockBean RenglonTraspasoRepository renglonTraspasoRepository;
  @MockBean MessageSource messageSource;

  @Autowired TraspasoServiceImpl traspasoService;

  @Test
  void shouldGuardarTraspasosPorPedido() {
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    sucursal.setNombre("sucursal uno");
    Sucursal sucursal2 = new Sucursal();
    sucursal2.setIdSucursal(2L);
    sucursal2.setNombre("Sucursal dos");
    Sucursal sucursal3 = new Sucursal();
    sucursal3.setIdSucursal(3L);
    sucursal3.setNombre("Sucursal tres");
    List<Sucursal> sucursales = new ArrayList<>();
    sucursales.add(sucursal);
    sucursales.add(sucursal2);
    sucursales.add(sucursal3);
    when(sucursalService.getSucusales(false)).thenReturn(sucursales);
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    when(sucursalService.getSucursalPorId(2L)).thenReturn(sucursal2);
    when(sucursalService.getSucursalPorId(3L)).thenReturn(sucursal3);
    ProductoFaltanteDTO productoFaltanteDTO1 =
        ProductoFaltanteDTO.builder()
            .idProducto(1L)
            .cantidadDisponible(BigDecimal.TEN)
            .cantidadSolicitada(new BigDecimal("100"))
            .build();
    ProductoFaltanteDTO productoFaltanteDTO3 =
        ProductoFaltanteDTO.builder()
            .idProducto(3L)
            .cantidadDisponible(BigDecimal.ZERO)
            .cantidadSolicitada(new BigDecimal("20"))
            .build();
    ProductoFaltanteDTO productoFaltanteDTO4 =
        ProductoFaltanteDTO.builder()
            .idProducto(4L)
            .cantidadDisponible(new BigDecimal("50"))
            .cantidadSolicitada(new BigDecimal("150"))
            .build();
    List<ProductoFaltanteDTO> productosFaltantes = new ArrayList<>();
    productosFaltantes.add(productoFaltanteDTO1);
    productosFaltantes.add(productoFaltanteDTO3);
    productosFaltantes.add(productoFaltanteDTO4);
    when(productoService.getProductosSinStockDisponible(any())).thenReturn(productosFaltantes);
    Pedido pedido = new Pedido();
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    RenglonPedido renglonPedido1 = new RenglonPedido();
    renglonPedido1.setIdProductoItem(1L);
    renglonPedido1.setCantidad(new BigDecimal("100"));
    RenglonPedido renglonPedido2 = new RenglonPedido();
    renglonPedido2.setIdProductoItem(2L);
    renglonPedido2.setCantidad(new BigDecimal("50"));
    RenglonPedido renglonPedido3 = new RenglonPedido();
    renglonPedido3.setIdProductoItem(3L);
    renglonPedido3.setCantidad(new BigDecimal("20"));
    RenglonPedido renglonPedido4 = new RenglonPedido();
    renglonPedido3.setIdProductoItem(4L);
    renglonPedido3.setCantidad(new BigDecimal("150"));
    renglonesPedido.add(renglonPedido1);
    renglonesPedido.add(renglonPedido2);
    renglonesPedido.add(renglonPedido3);
    renglonesPedido.add(renglonPedido4);
    pedido.setSucursal(sucursal);
    pedido.setRenglones(renglonesPedido);
    Usuario usuario = new Usuario();
    usuario.setIdUsuario(1L);
    pedido.setUsuario(usuario);
    Medida medida = new Medida();
    medida.setNombre("Metro");
    Producto producto1 = new Producto();
    producto1.setIdProducto(1L);
    producto1.setMedida(medida);
    producto1.setCantidadTotalEnSucursales(new BigDecimal("101"));
    Set<CantidadEnSucursal> cantidadEnSucursalesProducto1 = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursal1Producto1 = new CantidadEnSucursal();
    cantidadEnSucursal1Producto1.setSucursal(sucursal);
    cantidadEnSucursal1Producto1.setCantidad(BigDecimal.TEN);
    CantidadEnSucursal cantidadEnSucursal2Producto1 = new CantidadEnSucursal();
    cantidadEnSucursal2Producto1.setSucursal(sucursal2);
    cantidadEnSucursal2Producto1.setCantidad(new BigDecimal("91"));
    CantidadEnSucursal cantidadEnSucursal3Producto1 = new CantidadEnSucursal();
    cantidadEnSucursal3Producto1.setSucursal(sucursal3);
    cantidadEnSucursal3Producto1.setCantidad(BigDecimal.ZERO);
    cantidadEnSucursalesProducto1.add(cantidadEnSucursal1Producto1);
    cantidadEnSucursalesProducto1.add(cantidadEnSucursal2Producto1);
    cantidadEnSucursalesProducto1.add(cantidadEnSucursal3Producto1);
    producto1.setCantidadEnSucursales(cantidadEnSucursalesProducto1);
    Producto producto2 = new Producto();
    producto2.setIdProducto(2L);
    producto2.setMedida(medida);
    producto2.setCantidadTotalEnSucursales(new BigDecimal("80"));
    Set<CantidadEnSucursal> cantidadEnSucursalesProducto2 = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursal1Producto2 = new CantidadEnSucursal();
    cantidadEnSucursal1Producto2.setSucursal(sucursal);
    cantidadEnSucursal1Producto2.setCantidad(new BigDecimal("60"));
    CantidadEnSucursal cantidadEnSucursal2Producto2 = new CantidadEnSucursal();
    cantidadEnSucursal2Producto2.setSucursal(sucursal2);
    cantidadEnSucursal2Producto2.setCantidad(new BigDecimal("20"));
    CantidadEnSucursal cantidadEnSucursal3Producto2 = new CantidadEnSucursal();
    cantidadEnSucursal3Producto2.setSucursal(sucursal3);
    cantidadEnSucursal3Producto2.setCantidad(BigDecimal.ZERO);
    cantidadEnSucursalesProducto2.add(cantidadEnSucursal1Producto2);
    cantidadEnSucursalesProducto2.add(cantidadEnSucursal2Producto2);
    cantidadEnSucursalesProducto2.add(cantidadEnSucursal3Producto2);
    producto2.setCantidadEnSucursales(cantidadEnSucursalesProducto2);
    Producto producto3 = new Producto();
    producto3.setIdProducto(3L);
    producto3.setMedida(medida);
    producto3.setCantidadTotalEnSucursales(new BigDecimal("20"));
    Set<CantidadEnSucursal> cantidadEnSucursalesProducto3 = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursal1Producto3 = new CantidadEnSucursal();
    cantidadEnSucursal1Producto3.setSucursal(sucursal);
    cantidadEnSucursal1Producto3.setCantidad(BigDecimal.ZERO);
    CantidadEnSucursal cantidadEnSucursal2Producto3 = new CantidadEnSucursal();
    cantidadEnSucursal2Producto3.setSucursal(sucursal2);
    cantidadEnSucursal2Producto3.setCantidad(new BigDecimal("20"));
    CantidadEnSucursal cantidadEnSucursal3Producto3 = new CantidadEnSucursal();
    cantidadEnSucursal3Producto3.setSucursal(sucursal3);
    cantidadEnSucursal3Producto3.setCantidad(BigDecimal.ZERO);
    cantidadEnSucursalesProducto3.add(cantidadEnSucursal1Producto3);
    cantidadEnSucursalesProducto3.add(cantidadEnSucursal2Producto3);
    cantidadEnSucursalesProducto3.add(cantidadEnSucursal3Producto3);
    producto3.setCantidadEnSucursales(cantidadEnSucursalesProducto3);
    Producto producto4 = new Producto();
    producto4.setIdProducto(4L);
    producto4.setMedida(medida);
    producto4.setCantidadTotalEnSucursales(new BigDecimal("150"));
    Set<CantidadEnSucursal> cantidadEnSucursalesProducto4 = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursal1Producto4 = new CantidadEnSucursal();
    cantidadEnSucursal1Producto4.setSucursal(sucursal);
    cantidadEnSucursal1Producto4.setCantidad(new BigDecimal("50"));
    CantidadEnSucursal cantidadEnSucursal2Producto4 = new CantidadEnSucursal();
    cantidadEnSucursal2Producto4.setSucursal(sucursal2);
    cantidadEnSucursal2Producto4.setCantidad(new BigDecimal("50"));
    CantidadEnSucursal cantidadEnSucursal3Producto4 = new CantidadEnSucursal();
    cantidadEnSucursal3Producto4.setSucursal(sucursal3);
    cantidadEnSucursal3Producto4.setCantidad(new BigDecimal("50"));
    cantidadEnSucursalesProducto4.add(cantidadEnSucursal1Producto4);
    cantidadEnSucursalesProducto4.add(cantidadEnSucursal2Producto4);
    cantidadEnSucursalesProducto4.add(cantidadEnSucursal3Producto4);
    producto4.setCantidadEnSucursales(cantidadEnSucursalesProducto4);
    when(productoService.getProductoNoEliminadoPorId(1L)).thenReturn(producto1);
    when(productoService.getProductoNoEliminadoPorId(2L)).thenReturn(producto2);
    when(productoService.getProductoNoEliminadoPorId(3L)).thenReturn(producto3);
    when(productoService.getProductoNoEliminadoPorId(4L)).thenReturn(producto4);
    List<NuevoTraspasoDTO> nuevosTraspasos =
        traspasoService.construirNuevosTraspasosPorPedido(pedido);
    assertEquals(2, nuevosTraspasos.size());
    Set<Long> idsEsperados = new HashSet<>();
    idsEsperados.add(1L);
    idsEsperados.add(3L);
    idsEsperados.add(4L);
    assertEquals(idsEsperados, nuevosTraspasos.get(0).getIdProductoConCantidad().keySet());
    assertEquals(new BigDecimal("90"), nuevosTraspasos.get(0).getIdProductoConCantidad().get(1L));
    assertEquals(new BigDecimal("20"), nuevosTraspasos.get(0).getIdProductoConCantidad().get(3L));
    assertEquals(new BigDecimal("50"), nuevosTraspasos.get(0).getIdProductoConCantidad().get(4L));
    idsEsperados.clear();
    idsEsperados.add(4L);
    assertEquals(idsEsperados, nuevosTraspasos.get(1).getIdProductoConCantidad().keySet());
    assertEquals(new BigDecimal("50"), nuevosTraspasos.get(1).getIdProductoConCantidad().get(4L));
    traspasoService.guardarTraspasosPorPedido(pedido);
    verify(messageSource, times(2)).getMessage(eq("mensaje_traspaso_realizado"), any(), any());
    verify(traspasoRepository, times(2)).save(any());
  }

  @Test
  void shouldEliminarTraspaso() {
    Traspaso traspaso = new Traspaso();
    when(traspasoRepository.findById(1L)).thenReturn(Optional.of(traspaso));
    traspasoService.eliminar(1L);
    verify(traspasoRepository, times(1)).delete(traspaso);
  }

  @Test
  void shouldNotGetTraspasoNoEliminadoPorId() {
    when(traspasoRepository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(
        EntityNotFoundException.class, () -> traspasoService.getTraspasoNoEliminadoPorid(1L));
    verify(messageSource).getMessage(eq("mensaje_traspaso_no_existente"), any(), any());
  }

  @Test
  void shouldTestBusquedaTraspasoCriteria() {
    BusquedaTraspasoCriteria criteria =
        BusquedaTraspasoCriteria.builder()
            .idSucursalOrigen(1L)
            .idSucursalDestino(2L)
            .fechaDesde(LocalDateTime.MIN)
            .fechaHasta(LocalDateTime.MIN)
            .idUsuario(7L)
            .nroTraspaso("334")
            .build();
    String resultadoBuilder =
        "traspaso.sucursalOrigen.idSucursal = 1 && traspaso.sucursalDestino.idSucursal = 2 "
            + "&& traspaso.fechaDeAlta between -999999999-01-01T00:00 and -999999999-01-01T23:59:59.999999999 "
            + "&& traspaso.usuario.idUsuario = 7 && traspaso.nroTraspaso = 334";
    assertEquals(resultadoBuilder, traspasoService.getBuilderTraspaso(criteria).toString());
    criteria =
        BusquedaTraspasoCriteria.builder()
            .idSucursalOrigen(1L)
            .idSucursalDestino(2L)
            .fechaDesde(LocalDateTime.MIN)
            .idUsuario(7L)
            .nroTraspaso("334")
            .build();
    resultadoBuilder =
        "traspaso.sucursalOrigen.idSucursal = 1 && traspaso.sucursalDestino.idSucursal = 2 "
            + "&& traspaso.fechaDeAlta > -999999999-01-01T00:00 "
            + "&& traspaso.usuario.idUsuario = 7 && traspaso.nroTraspaso = 334";
    assertEquals(resultadoBuilder, traspasoService.getBuilderTraspaso(criteria).toString());
    criteria =
        BusquedaTraspasoCriteria.builder()
            .idSucursalOrigen(1L)
            .idSucursalDestino(2L)
            .fechaHasta(LocalDateTime.MIN)
            .idUsuario(7L)
            .nroTraspaso("334")
            .build();
    resultadoBuilder =
        "traspaso.sucursalOrigen.idSucursal = 1 " +
                "&& traspaso.sucursalDestino.idSucursal = 2 " +
                "&& traspaso.fechaDeAlta < -999999999-01-01T23:59:59.999999999 " +
                "&& traspaso.usuario.idUsuario = 7 && traspaso.nroTraspaso = 334";
    assertEquals(resultadoBuilder, traspasoService.getBuilderTraspaso(criteria).toString());
  }

  @Test
  void shouldGetPageableTraspaso() {
    Pageable pageable = traspasoService.getPageable(0, null, null);
    assertEquals("fecha: DESC", pageable.getSort().toString());
    assertEquals(0, pageable.getPageNumber());
    pageable = traspasoService.getPageable(1, "sucursalOrigen.nombre", "ASC");
    assertEquals("sucursalOrigen.nombre: ASC", pageable.getSort().toString());
    assertEquals(1, pageable.getPageNumber());
    pageable = traspasoService.getPageable(3, "sucursalDestino.nombre", "DESC");
    assertEquals("sucursalDestino.nombre: DESC", pageable.getSort().toString());
    assertEquals(3, pageable.getPageNumber());
    pageable = traspasoService.getPageable(3, "sucursalDestino.nombre", "NO");
    assertEquals("sucursalDestino.nombre: DESC", pageable.getSort().toString());
    assertEquals(3, pageable.getPageNumber());
  }
}
