package sic.controller;

import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.*;
import sic.modelo.dto.CantidadEnSucursalDTO;
import sic.modelo.dto.ProductoDTO;
import sic.modelo.embeddable.CantidadProductoEmbeddable;
import sic.modelo.embeddable.PrecioProductoEmbeddable;
import sic.service.*;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ProductoController.class})
class ProductoControllerTest {

  @MockBean ProductoServiceImpl productoService;
  @MockBean MedidaServiceImpl medidaService;
  @MockBean RubroServiceImpl rubroService;
  @MockBean ProveedorServiceImpl proveedorService;
  @MockBean SucursalServiceImpl sucursalService;
  @MockBean AuthServiceImpl authService;
  @MockBean UsuarioServiceImpl usuarioService;
  @MockBean ModelMapper modelMapper;

  @Autowired ProductoController productoController;

  @Test
  void shouldActualizarProducto() {
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    Producto productoPersistido = new Producto();
    Set<CantidadEnSucursal> cantidadEnSucursales = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursal = new CantidadEnSucursal();
    cantidadEnSucursal.setCantidad(BigDecimal.TEN);
    cantidadEnSucursal.setSucursal(sucursal);
    cantidadEnSucursales.add(cantidadEnSucursal);
    cantidadEnSucursal.setSucursal(sucursal);
    CantidadProductoEmbeddable cantidadProductoEmbeddable = new CantidadProductoEmbeddable();
    cantidadProductoEmbeddable.setCantidadEnSucursales(cantidadEnSucursales);
    cantidadProductoEmbeddable.setCantidadTotalEnSucursales(BigDecimal.TEN);
    PrecioProductoEmbeddable precioProductoEmbeddable = new PrecioProductoEmbeddable();
    productoPersistido.setCantidadProducto(cantidadProductoEmbeddable);
    productoPersistido.setPrecioProducto(precioProductoEmbeddable);
    ProductoDTO productoDTO = new ProductoDTO();
    productoDTO.setIdProducto(1L);
    Set<CantidadEnSucursalDTO> cantidadesEnSucursales = new HashSet<>();
    CantidadEnSucursalDTO cantidadEnSucursalDTO =
        CantidadEnSucursalDTO.builder().idSucursal(1L).cantidad(BigDecimal.TEN).build();
    cantidadesEnSucursales.add(cantidadEnSucursalDTO);
    productoDTO.setCantidadEnSucursales(cantidadesEnSucursales);
    when(productoService.construirCantidadProductoEmbeddable(productoDTO)).thenReturn(cantidadProductoEmbeddable);
    when(productoService.construirPrecioProductoEmbeddable(productoDTO)).thenReturn(precioProductoEmbeddable);
    when(productoService.getProductoNoEliminadoPorId(1L)).thenReturn(productoPersistido);
    when(medidaService.getMedidaNoEliminadaPorId(1L)).thenReturn(new Medida());
    when(rubroService.getRubroNoEliminadoPorId(1L)).thenReturn(new Rubro());
    when(proveedorService.getProveedorNoEliminadoPorId(1L)).thenReturn(new Proveedor());
    Producto productoPorActualizar = new Producto();
    productoPorActualizar.setIdProducto(1L);
    productoPorActualizar.setCantidadProducto(new CantidadProductoEmbeddable());
    productoPorActualizar.setPrecioProducto(new PrecioProductoEmbeddable());
    when(modelMapper.map(productoDTO, Producto.class)).thenReturn(productoPorActualizar);
    when(modelMapper.map(cantidadEnSucursalDTO, CantidadEnSucursal.class))
        .thenReturn(cantidadEnSucursal);
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    Usuario usuario = new Usuario();
    usuario.setUsername("usuario");
    usuario.setRoles(Collections.emptyList());
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    productoController.actualizar(productoDTO, 1L, 1L, 1L, "headers");
    verify(productoService).actualizar(any(), any(), any());
  }

  @Test
  void shouldMarcarComoFavorito() {
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    productoController.marcarComoFavorito(1L, "headers");
    verify(productoService).guardarProductoFavorito(1L, 1L);
  }

  @Test
  void shouldGetProductoFavoritosDelCliente() {
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    productoController.getProductosFavoritosDelCliente(1L,0, "headers");
    verify(productoService).getPaginaProductosFavoritosDelCliente(1L, 1L, 0);
  }

  @Test
  void shouldQuitarProductoDeFavoritos() {
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    productoController.quitarProductoDeFavoritos(4L, "headers");
    verify(productoService).quitarProductoDeFavoritos(1L, 4L);
  }

  @Test
  void shouldQuitarProductosDeFavoritos() {
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    productoController.quitarProductosDeFavoritos("headers");
    verify(productoService).quitarProductosDeFavoritos(1L);
  }

  @Test
  void shouldGetCantidadDeProductosFavoritos() {
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    productoController.getCantidadDeProductosFavoritos("headers");
    verify(productoService).getCantidadDeProductosFavoritos(1L);
  }

  @Test
  void shouldGetProductosRelacionados() {
    productoController.getProductosRecomendados(1L, 1L, 0);
    verify(productoService).getProductosRelacionados(1L, 1L, 0);
  }
}
