package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import sic.interceptor.JwtInterceptor;
import sic.modelo.*;
import sic.modelo.dto.NuevoProductoDTO;
import sic.repository.ProductoRepository;
import sic.repository.ProveedorRepository;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ProductoServiceImpl2Test {

  @Autowired ProveedorServiceImpl proveedorService;
  @Autowired MedidaServiceImpl medidaService;
  @Autowired RubroServiceImpl rubroService;
  @Autowired SucursalServiceImpl sucursalService;
  @Autowired ProductoServiceImpl productoService;

  @Test
  @Transactional
  void shouldTestActualizarStockPedido() {
    Proveedor proveedor = new Proveedor();
    proveedor.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    proveedor.setNroProveedor("123");
    proveedor.setRazonSocial("test proveedor");
    proveedor = proveedorService.guardar(proveedor);
    Medida medida = new Medida();
    medida.setNombre("Metro");
    medida = medidaService.guardar(medida);
    Rubro rubro = new Rubro();
    rubro.setNombre("rubro test");
    rubro = rubroService.guardar(rubro);
    Sucursal sucursal = new Sucursal();
    sucursal.setNombre("sucursal test");
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    sucursal.setEmail("asd@asd.com");
    sucursal = sucursalService.guardar(sucursal);
    NuevoProductoDTO nuevoProducto =
        NuevoProductoDTO.builder()
            .descripcion("Corta Papas - Vegetales")
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
            .ivaPorcentaje(new BigDecimal("10.5"))
            .ivaNeto(new BigDecimal("105"))
            .precioLista(new BigDecimal("1105"))
            .porcentajeBonificacionPrecio(new BigDecimal("20"))
            .publico(true)
            .build();
    Producto producto =
        productoService.guardar(
            nuevoProducto, medida.getIdMedida(), rubro.getIdRubro(), proveedor.getIdProveedor());
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
