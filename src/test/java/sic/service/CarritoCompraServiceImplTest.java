package sic.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.ItemCarritoCompra;
import sic.modelo.Producto;
import sic.modelo.Sucursal;
import sic.modelo.Usuario;
import sic.modelo.dto.CarritoCompraDTO;
import sic.modelo.dto.ProductosParaVerificarStockDTO;
import sic.modelo.embeddable.CantidadProductoEmbeddable;
import sic.modelo.embeddable.PrecioProductoEmbeddable;
import sic.repository.CarritoCompraRepository;
import sic.util.CustomValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CustomValidator.class, CarritoCompraServiceImpl.class})
class CarritoCompraServiceImplTest {

  @MockBean CarritoCompraRepository carritoCompraRepository;
  @MockBean
  UsuarioService usuarioService;
  @MockBean
  ProductoService productoService;
  @MockBean
  SucursalService sucursalService;
  @MockBean
  ClienteService clienteService;
  @MockBean
  PedidoService pedidoService;

  @Autowired CarritoCompraServiceImpl carritoCompraServiceImpl;

  @Test
  void shouldGetCarritoDeCompra() {
    when(carritoCompraRepository.getCantArticulos(1L)).thenReturn(new BigDecimal("3"));
    when(carritoCompraRepository.getCantRenglones(1L)).thenReturn(3L);
    Usuario usuario = new Usuario();
    usuario.setIdUsuario(1L);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    Producto producto1 = new Producto();
    producto1.setCantidadProducto(new CantidadProductoEmbeddable());
    producto1.getCantidadProducto().setCantMinima(BigDecimal.TEN);
    producto1.setPrecioProducto(new PrecioProductoEmbeddable());
    producto1.getPrecioProducto().setPrecioLista(new BigDecimal("200"));
    Producto producto2 = new Producto();
    producto2.setCantidadProducto(new CantidadProductoEmbeddable());
    producto2.getCantidadProducto().setCantMinima(BigDecimal.TEN);
    producto2.setPrecioProducto(new PrecioProductoEmbeddable());
    producto2.getPrecioProducto().setPrecioLista(new BigDecimal("350"));
    Producto producto3 = new Producto();
    producto3.setCantidadProducto(new CantidadProductoEmbeddable());
    producto3.getCantidadProducto().setCantMinima(BigDecimal.TEN);
    producto3.setPrecioProducto(new PrecioProductoEmbeddable());
    producto3.getPrecioProducto().setPrecioLista(new BigDecimal("900"));
    List<ItemCarritoCompra> itemsCarritoCompra = new ArrayList<>();
    ItemCarritoCompra itemCarritoCompra1 = new ItemCarritoCompra();
    itemCarritoCompra1.setImporte(new BigDecimal("200"));
    itemCarritoCompra1.setCantidad(BigDecimal.ONE);
    itemCarritoCompra1.setProducto(producto1);
    ItemCarritoCompra itemCarritoCompra2 = new ItemCarritoCompra();
    itemCarritoCompra2.setImporte(new BigDecimal("350"));
    itemCarritoCompra2.setCantidad(BigDecimal.ONE);
    itemCarritoCompra2.setProducto(producto2);
    ItemCarritoCompra itemCarritoCompra3 = new ItemCarritoCompra();
    itemCarritoCompra3.setCantidad(BigDecimal.ONE);
    itemCarritoCompra3.setImporte(new BigDecimal("900"));
    itemCarritoCompra3.setProducto(producto3);
    itemsCarritoCompra.add(itemCarritoCompra1);
    itemsCarritoCompra.add(itemCarritoCompra2);
    itemsCarritoCompra.add(itemCarritoCompra3);
    Page<ItemCarritoCompra> itemsCarritoCompras = new PageImpl<>(itemsCarritoCompra);
    when(carritoCompraRepository.findAllByUsuario(
            usuario,
            PageRequest.of(
                0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "idItemCarritoCompra"))))
        .thenReturn(itemsCarritoCompras);
    CarritoCompraDTO carritoCompra = carritoCompraServiceImpl.getCarritoCompra(1L);
    assertNotNull(carritoCompra);
    assertEquals(
        new BigDecimal("3"), carritoCompraServiceImpl.getCarritoCompra(1L).getCantArticulos());
    assertEquals(3, carritoCompraServiceImpl.getCarritoCompra(1L).getCantRenglones());
    assertEquals(
        new BigDecimal("1450.00"), carritoCompraServiceImpl.getCarritoCompra(1L).getTotal());
  }

  @Test
  void shouldGetProductosDelCarritoSinStockDisponible() {
    Producto producto1 = new Producto();
    producto1.setIdProducto(1L);
    producto1.setCantidadProducto(new CantidadProductoEmbeddable());
    producto1.getCantidadProducto().setCantMinima(BigDecimal.TEN);
    producto1.setPrecioProducto(new PrecioProductoEmbeddable());
    producto1.getPrecioProducto().setPrecioLista(new BigDecimal("200"));
    Producto producto2 = new Producto();
    producto2.setIdProducto(2L);
    producto2.setCantidadProducto(new CantidadProductoEmbeddable());
    producto2.getCantidadProducto().setCantMinima(BigDecimal.TEN);
    producto2.setPrecioProducto(new PrecioProductoEmbeddable());
    producto2.getPrecioProducto().setPrecioLista(new BigDecimal("350"));
    Producto producto3 = new Producto();
    producto3.setIdProducto(3L);
    producto3.setCantidadProducto(new CantidadProductoEmbeddable());
    producto3.getCantidadProducto().setCantMinima(BigDecimal.TEN);
    producto3.setPrecioProducto(new PrecioProductoEmbeddable());
    producto3.getPrecioProducto().setPrecioLista(new BigDecimal("900"));
    List<ItemCarritoCompra> itemsCarritoCompra = new ArrayList<>();
    ItemCarritoCompra itemCarritoCompra1 = new ItemCarritoCompra();
    itemCarritoCompra1.setImporte(new BigDecimal("200"));
    itemCarritoCompra1.setCantidad(BigDecimal.ONE);
    itemCarritoCompra1.setProducto(producto1);
    ItemCarritoCompra itemCarritoCompra2 = new ItemCarritoCompra();
    itemCarritoCompra2.setImporte(new BigDecimal("350"));
    itemCarritoCompra2.setCantidad(BigDecimal.ONE);
    itemCarritoCompra2.setProducto(producto2);
    ItemCarritoCompra itemCarritoCompra3 = new ItemCarritoCompra();
    itemCarritoCompra3.setCantidad(BigDecimal.ONE);
    itemCarritoCompra3.setImporte(new BigDecimal("900"));
    itemCarritoCompra3.setProducto(producto3);
    itemsCarritoCompra.add(itemCarritoCompra1);
    itemsCarritoCompra.add(itemCarritoCompra2);
    itemsCarritoCompra.add(itemCarritoCompra3);
    Usuario usuario = new Usuario();
    usuario.setIdUsuario(1L);
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    when(carritoCompraRepository.findAllByUsuarioOrderByIdItemCarritoCompraDesc(usuario))
        .thenReturn(itemsCarritoCompra);
    long[] idProducto = new long[itemsCarritoCompra.size()];
    BigDecimal[] cantidad = new BigDecimal[itemsCarritoCompra.size()];
    int indice = 0;
    for (ItemCarritoCompra item : itemsCarritoCompra) {
      idProducto[indice] = item.getProducto().getIdProducto();
      cantidad[indice] = item.getCantidad();
      indice++;
    }
    ProductosParaVerificarStockDTO productosParaVerificarStockDTO =
        ProductosParaVerificarStockDTO.builder().idSucursal(1L).idProducto(idProducto).cantidad(cantidad).build();
    carritoCompraServiceImpl.getProductosDelCarritoSinStockDisponible(1L, 1L);
    verify(productoService, times(1))
        .getProductosSinStockDisponible(productosParaVerificarStockDTO);
  }
}
