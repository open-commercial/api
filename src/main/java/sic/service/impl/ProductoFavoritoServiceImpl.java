package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sic.modelo.*;
import sic.repository.ProductoFavoritoRepository;
import sic.service.IClienteService;
import sic.service.IProductoFavoritoService;
import sic.service.IProductoService;
import sic.util.CustomValidator;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ProductoFavoritoServiceImpl implements IProductoFavoritoService {

  private final ProductoFavoritoRepository productoFavoritoRepository;
  private final IClienteService clienteService;
  private final IProductoService productoService;
  private final MessageSource messageSource;
  private final CustomValidator customValidator;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;

  @Autowired
  public ProductoFavoritoServiceImpl(
      ProductoFavoritoRepository productoFavoritoRepository,
      IClienteService clienteService,
      IProductoService productoService,
      MessageSource messageSource,
      CustomValidator customValidator) {
    this.productoFavoritoRepository = productoFavoritoRepository;
    this.clienteService = clienteService;
    this.productoService = productoService;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  @Override
  public ProductoFavorito getProductoFavoritoPorId(Long idProductoFavorito) {
    Optional<ProductoFavorito> productoFavorito = productoFavoritoRepository.findById(idProductoFavorito);
    if (productoFavorito.isPresent()) {
      return productoFavorito.get();
    } else {
      throw new EntityNotFoundException(messageSource.getMessage(
              "mensaje_cliente_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public ProductoFavorito guardarProductoFavorito(long idProducto, long idCliente) {
    Producto producto = productoService.getProductoNoEliminadoPorId(idProducto);
    Cliente cliente = clienteService.getClienteNoEliminadoPorId(idCliente);
    ProductoFavorito productoFavorito = new ProductoFavorito();
    productoFavorito.setCliente(cliente);
    productoFavorito.setProducto(producto);
    customValidator.validar(productoFavorito);
    return productoFavoritoRepository.save(productoFavorito);
  }

  @Override
  public Page<Producto> getProductosFavoritosDelCliente(long idCliente, int pagina) {
    //builder
    //QProductoFavorito qProductoFavorito = QProductoFavorito.productoFavorito;
    BooleanBuilder builder = new BooleanBuilder();
//    PageRequest.of(
//            pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, "idProductoFavorito"));
//    return productoFavoritoRepository.findAll(builder, PageRequest.of(
//            pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, "idProductoFavorito")));//predicate - pageable
//    //
//    List<ProductoFavorito> productosFavoritos =
//        productoFavoritoRepository.findByCliente(
//            clienteService.getClienteNoEliminadoPorId(idCliente));
//    List<Producto> productos = new ArrayList<>();
//    productosFavoritos.forEach(productoFavorito -> productos.add(productoFavorito.getProducto()));

    return null;
  }

  @Override
  public void quitarProductoDeFavoritos(long idProductoFavorito) {
    productoFavoritoRepository.delete(
        productoFavoritoRepository.findById(idProductoFavorito).get());
  }
}
