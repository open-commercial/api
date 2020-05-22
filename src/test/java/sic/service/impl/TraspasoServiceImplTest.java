package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.*;
import sic.modelo.dto.NuevoTraspasoDTO;
import sic.modelo.dto.ProductoFaltanteDTO;
import sic.modelo.dto.ProductosParaVerificarStockDTO;
import sic.repository.TraspasoRepository;
import sic.util.CustomValidator;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CustomValidator.class, TraspasoServiceImpl.class})
public class TraspasoServiceImplTest {

  @MockBean ProductoServiceImpl productoService;
  @MockBean SucursalServiceImpl sucursalService;
  @MockBean UsuarioServiceImpl usuarioService;
  @MockBean TraspasoRepository traspasoRepository;

  @Autowired TraspasoServiceImpl traspasoService;

  @Test
  void shouldGuardarTraspasosPorPedido() {
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    sucursal.setNombre("sucursal uno");
    Sucursal sucursal2 = new Sucursal();
    sucursal2.setIdSucursal(2L);
    sucursal2.setNombre("Sucursal dos");
    List<Sucursal> sucursales = new ArrayList<>();
    sucursales.add(sucursal);
    sucursales.add(sucursal2);
    when(sucursalService.getSucusales(false)).thenReturn(sucursales);
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    when(sucursalService.getSucursalPorId(2L)).thenReturn(sucursal2);
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
    List<ProductoFaltanteDTO> productosFaltantes = new ArrayList<>();
    productosFaltantes.add(productoFaltanteDTO1);
    productosFaltantes.add(productoFaltanteDTO3);
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
    renglonesPedido.add(renglonPedido1);
    renglonesPedido.add(renglonPedido2);
    renglonesPedido.add(renglonPedido3);
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
    producto1.setCantidadTotalEnSucursales(new BigDecimal("100"));
    Set<CantidadEnSucursal> cantidadEnSucursalesProducto1 = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursalSucursal1Producto1 = new CantidadEnSucursal();
    cantidadEnSucursalSucursal1Producto1.setSucursal(sucursal);
    cantidadEnSucursalSucursal1Producto1.setCantidad(BigDecimal.TEN);
    CantidadEnSucursal cantidadEnSucursalSucursal2Producto1 = new CantidadEnSucursal();
    cantidadEnSucursalSucursal2Producto1.setSucursal(sucursal2);
    cantidadEnSucursalSucursal2Producto1.setCantidad(new BigDecimal("90"));
    cantidadEnSucursalesProducto1.add(cantidadEnSucursalSucursal1Producto1);
    cantidadEnSucursalesProducto1.add(cantidadEnSucursalSucursal2Producto1);
    producto1.setCantidadEnSucursales(cantidadEnSucursalesProducto1);
    Producto producto2 = new Producto();
    producto2.setIdProducto(2L);
    producto2.setMedida(medida);
    producto2.setCantidadTotalEnSucursales(new BigDecimal("80"));
    Set<CantidadEnSucursal> cantidadEnSucursalesProducto2 = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursalSucursal1Producto2 = new CantidadEnSucursal();
    cantidadEnSucursalSucursal1Producto2.setSucursal(sucursal);
    cantidadEnSucursalSucursal1Producto2.setCantidad(new BigDecimal("60"));
    CantidadEnSucursal cantidadEnSucursalSucursal2Producto2 = new CantidadEnSucursal();
    cantidadEnSucursalSucursal2Producto2.setSucursal(sucursal2);
    cantidadEnSucursalSucursal2Producto2.setCantidad(new BigDecimal("20"));
    cantidadEnSucursalesProducto2.add(cantidadEnSucursalSucursal1Producto2);
    cantidadEnSucursalesProducto2.add(cantidadEnSucursalSucursal2Producto2);
    producto2.setCantidadEnSucursales(cantidadEnSucursalesProducto2);
    Producto producto3 = new Producto();
    producto3.setIdProducto(3L);
    producto3.setMedida(medida);
    producto3.setCantidadTotalEnSucursales(new BigDecimal("20"));
    Set<CantidadEnSucursal> cantidadEnSucursalesProducto3 = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursalSucursal1Producto3 = new CantidadEnSucursal();
    cantidadEnSucursalSucursal1Producto3.setSucursal(sucursal);
    cantidadEnSucursalSucursal1Producto3.setCantidad(BigDecimal.ZERO);
    CantidadEnSucursal cantidadEnSucursalSucursal2Producto3 = new CantidadEnSucursal();
    cantidadEnSucursalSucursal2Producto3.setSucursal(sucursal2);
    cantidadEnSucursalSucursal2Producto3.setCantidad(new BigDecimal("20"));
    cantidadEnSucursalesProducto3.add(cantidadEnSucursalSucursal1Producto3);
    cantidadEnSucursalesProducto3.add(cantidadEnSucursalSucursal2Producto3);
    producto3.setCantidadEnSucursales(cantidadEnSucursalesProducto3);
    when(productoService.getProductoNoEliminadoPorId(1L)).thenReturn(producto1);
    when(productoService.getProductoNoEliminadoPorId(2L)).thenReturn(producto2);
    when(productoService.getProductoNoEliminadoPorId(3L)).thenReturn(producto3);
    List<NuevoTraspasoDTO> nuevosTraspasos =
        traspasoService.construirNuevosTraspasosPorPedido(pedido);
    assertEquals(1, nuevosTraspasos.size());
    Set<Long> idsEsperados = new HashSet<>();
    idsEsperados.add(1L);
    idsEsperados.add(3L);
    assertEquals(idsEsperados, nuevosTraspasos.get(0).getProductosAndCantidades().keySet());
    assertEquals(new BigDecimal("90"), nuevosTraspasos.get(0).getProductosAndCantidades().get(1L));
    assertEquals(new BigDecimal("20"), nuevosTraspasos.get(0).getProductosAndCantidades().get(3L));
    traspasoService.guardarTraspasosPorPedido(pedido);
    verify(traspasoRepository, times(1)).save(any());
  }

  @Test
  void shouldEliminarTraspaso() {
    Traspaso traspaso = new Traspaso();
    when(traspasoRepository.findById(1L)).thenReturn(Optional.of(traspaso));
    traspasoService.eliminar(1L);
    verify(traspasoRepository, times(1)).delete(traspaso);
  }
}
