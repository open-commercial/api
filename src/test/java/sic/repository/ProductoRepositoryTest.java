package sic.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.App;
import sic.interceptor.JwtInterceptor;
import sic.modelo.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ExtendWith(SpringExtension.class)
// @SpringBootTest
// @ContextConfiguration(classes = {App.class})
public class ProductoRepositoryTest {

  @Autowired MedidaRepository medidaRepository;
  @Autowired ProveedorRepository proveedorRepository;
  @Autowired RubroRepository rubroRepository;
  @Autowired SucursalRepository sucursalRepository;
  @Autowired ProductoRepository productoRepository;

  @MockBean JwtInterceptor jwtInterceptor;

  @Autowired TestEntityManager testEntityManager;

  @Test
  @Disabled
  public void shouldTestConcurrenciaEnActualizacionDeProducto() {
    Assertions.assertThrows(
        ObjectOptimisticLockingFailureException.class,
        () -> {
          Proveedor proveedor = new Proveedor();
          proveedor.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
          proveedor.setNroProveedor("123");
          proveedor.setRazonSocial("test proveedor");
          proveedor = proveedorRepository.save(proveedor);
          Medida medida = new Medida();
          medida.setNombre("Metro");
          medida = medidaRepository.save(medida);
          Rubro rubro = new Rubro();
          rubro.setNombre("rubro test");
          rubro = rubroRepository.save(rubro);
          Sucursal sucursal = new Sucursal();
          sucursal.setNombre("sucursal test");
          sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
          sucursal.setEmail("asd@asd.com");
          sucursal = sucursalRepository.save(sucursal);
          Producto producto = new Producto();
          producto.setDescripcion("Producto para test");
          producto.setMedida(medida);
          producto.setProveedor(proveedor);
          producto.setRubro(rubro);
          producto.setBulto(BigDecimal.ONE);
          producto.setFechaAlta(LocalDateTime.now());
          producto.setFechaUltimaModificacion(LocalDateTime.now());

          Set<CantidadEnSucursal> cantidadEnSucursales = new HashSet<>();
          CantidadEnSucursal cantidadEnSucursal = new CantidadEnSucursal();
          cantidadEnSucursal.setCantidad(BigDecimal.ONE);
          cantidadEnSucursal.setSucursal(sucursal);
          cantidadEnSucursales.add(cantidadEnSucursal);
          producto.setCantidadEnSucursales(cantidadEnSucursales);
          producto.setCantidadTotalEnSucursales(
              producto.getCantidadEnSucursales().stream()
                  .map(CantidadEnSucursal::getCantidad)
                  .reduce(BigDecimal.ZERO, BigDecimal::add));

          producto = productoRepository.save(producto);
          assertNotNull(producto);
          Optional<Producto> productoDelUsuarioUno = productoRepository.findById(1L);
          Optional<Producto> productoDelUsuarioDos = productoRepository.findById(1L);
          assertEquals(productoDelUsuarioUno, productoDelUsuarioDos);
          productoDelUsuarioUno.ifPresent(p -> p.setDescripcion("Producto del usuario uno"));
          productoDelUsuarioDos.ifPresent(p -> p.setDescripcion("Producto del usuario dos"));
          productoDelUsuarioUno.ifPresent(p -> productoRepository.save(p));
          productoDelUsuarioDos.ifPresent(p -> productoRepository.save(p));
        });
  }
}
