package sic.service;

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
    public double getTotal(long idUsuario) {
        Double total = carritoCompraRepository.calcularTotal(idUsuario);
        if (total == null) {
            return 0;
        } else {
            return total;
        }        
    }

    @Override
    public double getCantArticulos(long idUsuario) {
        Double cantArticulos = carritoCompraRepository.getCantArticulos(idUsuario);
        if (cantArticulos == null) {
            return 0;
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
    public void agregarOrModificarItem(long idUsuario, long idProducto, double cantidad) {
        Usuario usuario = usuarioService.getUsuarioPorId(idUsuario);
        Producto producto = productoService.getProductoPorId(idProducto);
        ItemCarritoCompra item = carritoCompraRepository.findByUsuarioAndProducto(usuario, producto);
        if (item == null) {
            double importe = producto.getPrecioLista() * cantidad;
            carritoCompraRepository.save(new ItemCarritoCompra(null, cantidad, producto, importe, usuario));
        } else {
            double nuevaCantidad = item.getCantidad() + cantidad;
            if (nuevaCantidad < 0) {
                item.setCantidad(0);    
            } else {
                item.setCantidad(nuevaCantidad);    
            }            
            item.setImporte(producto.getPrecioLista() * nuevaCantidad);
            carritoCompraRepository.save(item);
        }
    }

}
