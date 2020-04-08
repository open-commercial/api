package sic.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import sic.modelo.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ProductoRepositoryTest {

  @Autowired MedidaRepository medidaRepository;

  @Autowired ProveedorRepository proveedorRepository;

  @Autowired RubroRepository rubroRepository;

  @Autowired SucursalRepository sucursalRepository;

  @Autowired ProductoRepository productoRepository;

  @Test(expected = ObjectOptimisticLockingFailureException.class)
  public void shouldTestConcurrenciaEnActualizacionDeProducto() {
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
    producto.setIdProducto(1L);
    producto.setCodigo("1");
    producto.setDescripcion("Producto para test");
    producto.setMedida(medida);
    producto.setProveedor(proveedor);
    producto.setRubro(rubro);
    producto.setPrecioCosto(new BigDecimal("89.35"));
    producto.setGananciaPorcentaje(new BigDecimal("38.74"));
    producto.setGananciaNeto(new BigDecimal("34.62"));
    producto.setPrecioVentaPublico(new BigDecimal("123.97"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    producto.setIvaNeto(new BigDecimal("26.03"));
    producto.setPrecioLista(new BigDecimal("150"));
    producto.setPorcentajeBonificacionPrecio(new BigDecimal("10"));
    producto.setPrecioBonificado(new BigDecimal("135"));
    producto.setPorcentajeBonificacionOferta(BigDecimal.ZERO);
    producto.setBulto(new BigDecimal("5"));
    producto.setFechaAlta(LocalDateTime.now());
    producto.setFechaUltimaModificacion(LocalDateTime.now());

    Set<CantidadEnSucursal> altaCantidadesEnSucursales = new HashSet<>();
    CantidadEnSucursal cantidad = new CantidadEnSucursal();
    cantidad.setCantidad(BigDecimal.ONE);
    cantidad.setSucursal(sucursal);
    altaCantidadesEnSucursales.add(cantidad);
    producto.setCantidadEnSucursales(altaCantidadesEnSucursales);
    producto.setCantidadTotalEnSucursales(
        producto.getCantidadEnSucursales().stream()
            .map(CantidadEnSucursal::getCantidad)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

    producto.setVersion(1L);
    productoRepository.save(producto);
    Optional<Producto> productoDelUsuarioUno = productoRepository.findById(1L);
    Optional<Producto> productoDelUsuarioDos = productoRepository.findById(1L);
    productoDelUsuarioUno.ifPresent(p -> p.setDescripcion("Producto del usuario uno"));
    productoDelUsuarioDos.ifPresent(p -> p.setDescripcion("Producto del usuario dos"));
    productoDelUsuarioUno.ifPresent(p -> productoRepository.save(p));
    productoDelUsuarioDos.ifPresent(p -> productoRepository.save(p));
  }
}
