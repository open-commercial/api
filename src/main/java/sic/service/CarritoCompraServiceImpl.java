package sic.service;

import java.math.BigDecimal;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.ItemCarritoCompra;
import sic.modelo.Producto;
import sic.modelo.Usuario;
import sic.repository.CarritoCompraRepository;

@Service
@Transactional
public class CarritoCompraServiceImpl implements ICarritoCompraService {

    private final CarritoCompraRepository carritoCompraRepository;
    private final IUsuarioService usuarioService;
    private final IProductoService productoService;    
    private static final Logger LOGGER = Logger.getLogger(CarritoCompraServiceImpl.class.getPackage().getName());

    @Autowired
    public CarritoCompraServiceImpl(CarritoCompraRepository carritoCompraRepository, IUsuarioService usuarioService,
            IProductoService productoService) {
        this.carritoCompraRepository = carritoCompraRepository;
        this.usuarioService = usuarioService;
        this.productoService = productoService;
    }

    @Override
    public Page<ItemCarritoCompra> getAllItemsDelUsuario(long idUsuario, Pageable pageable) {
        return carritoCompraRepository.findAllByUsuario(usuarioService.getUsuarioPorId(idUsuario), pageable);
    }

    @Override
    public BigDecimal getTotal(long idUsuario) {
        BigDecimal total = carritoCompraRepository.calcularTotal(idUsuario);
        if (total == null) {
            return BigDecimal.ZERO;
        } else {
            return total;
        }        
    }

    @Override
    public BigDecimal getCantArticulos(long idUsuario) {
        BigDecimal cantArticulos = carritoCompraRepository.getCantArticulos(idUsuario);
        if (cantArticulos == null) {
            return BigDecimal.ZERO;
        } else {
            return cantArticulos;
        }
    }
    
    @Override
    public long getCantRenglones(long idUsuario) {
        return carritoCompraRepository.getCantRenglones(idUsuario);
    }

    @Override
    public void eliminarItem(long idUsuario, long idProducto) {
        carritoCompraRepository.eliminarItem(idUsuario, idProducto);
    }

    @Override
    public void eliminarTodosLosItems(long idUsuario) {
        carritoCompraRepository.eliminarTodosLosItems(idUsuario);
    }

    @Override
    public void agregarOrModificarItem(long idUsuario, long idProducto, BigDecimal cantidad) {
        Usuario usuario = usuarioService.getUsuarioPorId(idUsuario);
        Producto producto = productoService.getProductoPorId(idProducto);
        ItemCarritoCompra item = carritoCompraRepository.findByUsuarioAndProducto(usuario, producto);
        if (item == null) {
            BigDecimal importe = producto.getPrecioLista().multiply(cantidad);
            carritoCompraRepository.save(new ItemCarritoCompra(null, cantidad, producto, importe, usuario));
        } else {
            BigDecimal nuevaCantidad = item.getCantidad().add(cantidad);
            if (nuevaCantidad.compareTo(BigDecimal.ZERO) < 0) {
                item.setCantidad(BigDecimal.ZERO);    
            } else {
                item.setCantidad(nuevaCantidad);    
            }            
            item.setImporte(producto.getPrecioLista().multiply(nuevaCantidad));
            carritoCompraRepository.save(item);
        }
    }

}
