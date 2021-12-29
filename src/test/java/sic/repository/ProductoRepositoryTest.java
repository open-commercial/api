package sic.repository;

import com.querydsl.core.BooleanBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.App;
import sic.interceptor.JwtInterceptor;
import sic.modelo.*;
import sic.modelo.embeddable.CantidadProductoEmbeddable;
import sic.modelo.embeddable.PrecioProductoEmbeddable;
import sic.repository.custom.ProductoRepositoryImpl;
import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ProductoRepositoryImpl.class, LocalidadRepository.class, App.class})
class ProductoRepositoryTest {

  @MockBean JwtInterceptor jwtInterceptor;

  @Autowired TestEntityManager testEntityManager;

  @Autowired ProductoRepositoryImpl productoRepositoryImpl;

  @Autowired LocalidadRepository localidadRepository;

  @Test
  void shouldThrowOptimisticLockExceptionWhenIntentaActualizarProductoDetached() {
    Assertions.assertThrows(
        OptimisticLockException.class,
        () -> {
          Proveedor proveedor = new Proveedor();
          proveedor.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
          proveedor.setNroProveedor("123");
          proveedor.setRazonSocial("test proveedor");
          proveedor = testEntityManager.persist(proveedor);
          Medida medida = new Medida();
          medida.setNombre("Metro");
          medida = testEntityManager.persist(medida);
          Rubro rubro = new Rubro();
          rubro.setNombre("rubro test");
          rubro = testEntityManager.persist(rubro);
          Sucursal sucursal = new Sucursal();
          sucursal.setNombre("sucursal test");
          sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
          sucursal.setEmail("asd@asd.com");
          Ubicacion ubicacionSucursal = new Ubicacion();
          ubicacionSucursal.setLocalidad(localidadRepository.findById(1L));
          sucursal.setUbicacion(ubicacionSucursal);
          sucursal = testEntityManager.persist(sucursal);
          Producto producto = new Producto();
          producto.setDescripcion("Producto para test");
          producto.setMedida(medida);
          producto.setProveedor(proveedor);
          producto.setRubro(rubro);
          producto.setCantidadProducto(new CantidadProductoEmbeddable());
          producto.getCantidadProducto().setCantMinima(BigDecimal.ONE);
          producto.setFechaAlta(LocalDateTime.now());
          producto.setFechaUltimaModificacion(LocalDateTime.now());
          Set<CantidadEnSucursal> cantidadEnSucursales = new HashSet<>();
          CantidadEnSucursal cantidadEnSucursal = new CantidadEnSucursal();
          cantidadEnSucursal.setCantidad(BigDecimal.ONE);
          cantidadEnSucursal.setSucursal(sucursal);
          cantidadEnSucursales.add(cantidadEnSucursal);
          producto.getCantidadProducto().setCantidadEnSucursales(cantidadEnSucursales);
          producto.getCantidadProducto().setCantidadReservada(BigDecimal.ZERO);
          producto
              .getCantidadProducto()
              .setCantidadTotalEnSucursales(
                  producto.getCantidadProducto().getCantidadEnSucursales().stream()
                      .map(CantidadEnSucursal::getCantidad)
                      .reduce(BigDecimal.ZERO, BigDecimal::add));
          producto.setPrecioProducto(new PrecioProductoEmbeddable());
          producto.getPrecioProducto().setOferta(false);
          Producto productoEnPrimeraInstancia = testEntityManager.persistFlushFind(producto);
          testEntityManager.detach(productoEnPrimeraInstancia);
          Producto productoEnSegundaInstancia =
              testEntityManager.find(Producto.class, productoEnPrimeraInstancia.getIdProducto());
          productoEnSegundaInstancia.setDescripcion("Nueva descripcion");
          testEntityManager.merge(productoEnSegundaInstancia);
          testEntityManager.flush();
          productoEnPrimeraInstancia.setCodigo("123");
          testEntityManager.merge(productoEnPrimeraInstancia);
        });
  }

  @Test
  void shouldTestCalcularValorStock() {
    Proveedor proveedor = new Proveedor();
    proveedor.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    proveedor.setNroProveedor("123");
    proveedor.setRazonSocial("test proveedor");
    proveedor = testEntityManager.persist(proveedor);
    Medida medida = new Medida();
    medida.setNombre("Metro");
    medida = testEntityManager.persist(medida);
    Rubro rubro = new Rubro();
    rubro.setNombre("rubro test");
    rubro = testEntityManager.persist(rubro);
    Sucursal sucursal = new Sucursal();
    sucursal.setNombre("sucursal test");
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    sucursal.setEmail("asd@asd.com");
    Ubicacion ubicacionSucursal = new Ubicacion();
    ubicacionSucursal.setLocalidad(localidadRepository.findById(1L));
    sucursal.setUbicacion(ubicacionSucursal);
    sucursal = testEntityManager.persist(sucursal);
    Producto producto = new Producto();
    producto.setDescripcion("Producto para test");
    producto.setMedida(medida);
    producto.setProveedor(proveedor);
    producto.setRubro(rubro);
    producto.setCantidadProducto(new CantidadProductoEmbeddable());
    producto.getCantidadProducto().setCantMinima(BigDecimal.ONE);
    producto.setFechaAlta(LocalDateTime.now());
    producto.setFechaUltimaModificacion(LocalDateTime.now());
    Set<CantidadEnSucursal> cantidadEnSucursales = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursal = new CantidadEnSucursal();
    cantidadEnSucursal.setCantidad(BigDecimal.ONE);
    cantidadEnSucursal.setSucursal(sucursal);
    cantidadEnSucursales.add(cantidadEnSucursal);
    producto.getCantidadProducto().setCantidadEnSucursales(cantidadEnSucursales);
    producto.getCantidadProducto().setCantidadReservada(BigDecimal.ZERO);
    producto
        .getCantidadProducto()
        .setCantidadTotalEnSucursales(
            producto.getCantidadProducto().getCantidadEnSucursales().stream()
                .map(CantidadEnSucursal::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    PrecioProductoEmbeddable precioProductoEmbeddable = new PrecioProductoEmbeddable();
    precioProductoEmbeddable.setPrecioCosto(BigDecimal.TEN);
    producto.setPrecioProducto(precioProductoEmbeddable);
    producto.getPrecioProducto().setOferta(false);
    testEntityManager.persist(producto);
    QProducto qProducto = QProducto.producto;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(qProducto.eliminado.eq(false));
    BigDecimal valorStock = productoRepositoryImpl.calcularValorStock(builder);
    assertEquals(10, valorStock.doubleValue());
  }
}
