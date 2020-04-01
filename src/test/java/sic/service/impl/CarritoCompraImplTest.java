package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.ItemCarritoCompra;
import sic.modelo.Producto;
import sic.modelo.Usuario;
import sic.modelo.dto.CarritoCompraDTO;
import sic.repository.CarritoCompraRepository;
import sic.service.IUsuarioService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CarritoCompraImplTest {

  @Mock CarritoCompraRepository carritoCompraRepository;
  @Mock IUsuarioService usuarioService;

  @InjectMocks CarritoCompraServiceImpl carritoCompraServiceImpl;

  @Test
  void shouldGetCarritoDeCompra() {
    when(carritoCompraRepository.getCantArticulos(1L)).thenReturn(new BigDecimal("3"));
    when(carritoCompraRepository.getCantRenglones(1L)).thenReturn(3L);
    Usuario usuario = new Usuario();
    usuario.setIdUsuario(1L);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    Producto producto1 = new Producto();
    producto1.setBulto(BigDecimal.TEN);
    producto1.setPrecioLista(new BigDecimal("200"));
    Producto producto2 = new Producto();
    producto2.setBulto(BigDecimal.TEN);
    producto2.setPrecioLista(new BigDecimal("350"));
    Producto producto3 = new Producto();
    producto3.setBulto(BigDecimal.TEN);
    producto3.setPrecioLista(new BigDecimal("900"));
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
    CarritoCompraDTO carritoCompra = carritoCompraServiceImpl.getCarritoCompra(1L, 1L);
    assertNotNull(carritoCompra);
    assertEquals(
        new BigDecimal("3"), carritoCompraServiceImpl.getCarritoCompra(1L, 1L).getCantArticulos());
    assertEquals(3, carritoCompraServiceImpl.getCarritoCompra(1L, 1L).getCantRenglones());
    assertEquals(
        new BigDecimal("1450.00"), carritoCompraServiceImpl.getCarritoCompra(1L, 1L).getTotal());
  }
}
