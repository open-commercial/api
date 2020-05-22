package sic.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.*;
import sic.modelo.dto.NuevoTraspasoDTO;
import sic.modelo.dto.ProductoFaltanteDTO;
import sic.modelo.dto.ProductosParaVerificarStockDTO;
import sic.repository.TraspasoRepository;
import sic.service.IProductoService;
import sic.service.ISucursalService;
import sic.service.ITraspasoService;
import sic.service.IUsuarioService;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class TraspasoServiceImpl implements ITraspasoService {

  private final TraspasoRepository traspasoRepository;
  private final IProductoService productoService;
  private final ISucursalService sucursalService;
  private final IUsuarioService usuarioService;
  private final MessageSource messageSource;

  @Autowired
  public TraspasoServiceImpl(
      TraspasoRepository traspasoRepository,
      IProductoService productoService,
      ISucursalService sucursalService,
      IUsuarioService usuarioService,
      MessageSource messageSource) {
    this.traspasoRepository = traspasoRepository;
    this.productoService = productoService;
    this.sucursalService = sucursalService;
    this.usuarioService = usuarioService;
    this.messageSource = messageSource;
  }

  @Override
  public Traspaso getTraspasoNoEliminadoPorid(Long idTraspaso) {
    Optional<Traspaso> traspaso = traspasoRepository.findById(idTraspaso);
    if (traspaso.isPresent() && !traspaso.get().isEliminado()) {
      return traspaso.get();
    } else {
      throw new EntityNotFoundException(
          messageSource.getMessage("mensaje_traspaso_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public Traspaso guardar(NuevoTraspasoDTO nuevoTraspasoDTO) {
    Traspaso traspaso = new Traspaso();
    traspaso.setFecha(LocalDateTime.now());
    traspaso.setNroTraspaso(this.generarNroDeTraspaso());
    Sucursal sucursalOrigen =
        sucursalService.getSucursalPorId(nuevoTraspasoDTO.getIdSucursalOrigen());
    Sucursal sucursalDestino =
        sucursalService.getSucursalPorId(nuevoTraspasoDTO.getIdSucursalDestino());
    traspaso.setSucursalOrigen(sucursalOrigen);
    traspaso.setSucursalDestino(sucursalDestino);
    traspaso.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(nuevoTraspasoDTO.getIdUsuario()));
    List<RenglonTraspaso> renglonesTraspaso = new ArrayList<>();
    nuevoTraspasoDTO
        .getProductosAndCantidades()
        .forEach(
            (idProducto, cantidad) -> {
              Producto producto = productoService.getProductoNoEliminadoPorId(idProducto);
              RenglonTraspaso renglonTraspaso = new RenglonTraspaso();
              renglonTraspaso.setIdProducto(producto.getIdProducto());
              renglonTraspaso.setCantidadTraspaso(cantidad);
              renglonTraspaso.setDescripcionTraspaso(producto.getDescripcion());
              renglonTraspaso.setNombreMedidaTraspaso(producto.getNombreMedida());
              renglonesTraspaso.add(renglonTraspaso);
            });
    traspaso.setRenglones(renglonesTraspaso);
    traspaso = traspasoRepository.save(traspaso);
    productoService.actualizarStockTraspaso(traspaso, TipoDeOperacion.ALTA);
    return traspaso;
  }

  @Override
  public List<Traspaso> guardarTraspasosPorPedido(Pedido pedido) {
    List<Traspaso> traspasos = new ArrayList<>();
    this.construirNuevosTraspasosPorPedido(pedido).stream()
        .filter(nuevoTraspaso -> !nuevoTraspaso.getProductosAndCantidades().isEmpty())
        .forEach(nuevoTraspaso -> traspasos.add(this.guardar(nuevoTraspaso)));
    return traspasos;
  }

  @Override
  public List<NuevoTraspasoDTO> construirNuevosTraspasosPorPedido(Pedido pedido) {
    long[] idProducto = new long[pedido.getRenglones().size()];
    BigDecimal[] cantidad = new BigDecimal[pedido.getRenglones().size()];
    int i = 0;
    for (RenglonPedido renglonPedido : pedido.getRenglones()) {
      idProducto[i] = renglonPedido.getIdProductoItem();
      cantidad[i] = renglonPedido.getCantidad();
      i++;
    }
    ProductosParaVerificarStockDTO productosParaVerificarStockDTO =
        ProductosParaVerificarStockDTO.builder()
            .cantidad(cantidad)
            .idProducto(idProducto)
            .idSucursal(pedido.getIdSucursal())
            .build();
    List<ProductoFaltanteDTO> faltantes =
        productoService.getProductosSinStockDisponible(productosParaVerificarStockDTO);
    if (!faltantes.isEmpty()) {
      List<Sucursal> sucursales = sucursalService.getSucusales(false);
      List<NuevoTraspasoDTO> nuevosTraspasos = new ArrayList<>();
      sucursales.stream()
          .filter(sucursal -> sucursal.getIdSucursal() != pedido.getSucursal().getIdSucursal())
          .forEach(
              sucursal -> {
                NuevoTraspasoDTO nuevoTraspasoDTO =
                    NuevoTraspasoDTO.builder()
                        .idSucursalOrigen(sucursal.getIdSucursal())
                        .idUsuario(pedido.getUsuario().getIdUsuario())
                        .productosAndCantidades(new HashMap<Long, BigDecimal>())
                        .build();
                nuevosTraspasos.add(nuevoTraspasoDTO);
              });
      for (ProductoFaltanteDTO productoFaltante : faltantes) {
        BigDecimal cantidadFaltante =
            productoFaltante
                .getCantidadSolicitada()
                .subtract(productoFaltante.getCantidadDisponible());
        Producto producto =
            productoService.getProductoNoEliminadoPorId(productoFaltante.getIdProducto());
        List<CantidadEnSucursal> listaOrdenadaPorCantidad =
            new ArrayList<>(producto.getCantidadEnSucursales());
        listaOrdenadaPorCantidad.sort(
            (cantidad1, cantidad2) -> cantidad2.getCantidad().compareTo(cantidad1.getCantidad()));
        for (CantidadEnSucursal cantidadEnSucursal : listaOrdenadaPorCantidad) {
          if (cantidadFaltante.compareTo(BigDecimal.ZERO) != 0) {
            if (cantidadFaltante.compareTo(cantidadEnSucursal.getCantidad()) <= 0) {
              cantidadFaltante = cantidadFaltante.subtract(cantidadEnSucursal.getCantidad());
              nuevosTraspasos.stream()
                  .filter(
                      nuevoTraspaso ->
                          nuevoTraspaso
                              .getIdSucursalOrigen()
                              .equals(cantidadEnSucursal.getIdSucursal()))
                  .forEach(
                      nuevoTraspaso -> {
                        nuevoTraspaso.setIdSucursalDestino(pedido.getSucursal().getIdSucursal());
                        nuevoTraspaso
                            .getProductosAndCantidades()
                            .put(
                                producto.getIdProducto(),
                                productoFaltante
                                    .getCantidadSolicitada()
                                    .subtract(productoFaltante.getCantidadDisponible()));
                      });
            } else {
              cantidadFaltante = cantidadFaltante.subtract(cantidadEnSucursal.getCantidad());
              nuevosTraspasos.stream()
                  .filter(
                      nuevoTraspaso ->
                          nuevoTraspaso
                              .getIdSucursalOrigen()
                              .equals(cantidadEnSucursal.getIdSucursal()))
                  .forEach(
                      nuevoTraspaso -> {
                        nuevoTraspaso.setIdSucursalDestino(pedido.getSucursal().getIdSucursal());
                        nuevoTraspaso
                            .getProductosAndCantidades()
                            .put(producto.getIdProducto(), cantidadEnSucursal.getCantidad());
                      });
            }
          }
        }
      }
      return nuevosTraspasos;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public void eliminar(Long idTraspaso) {
    Traspaso traspaso = this.getTraspasoNoEliminadoPorid(idTraspaso);
    productoService.actualizarStockTraspaso(traspaso, TipoDeOperacion.ELIMINACION);
    traspasoRepository.delete(traspaso);
  }

  @Override
  public String generarNroDeTraspaso() {
    long min = 1L;
    long max = 99999L; // 5 digitos
    long randomLong = 0L;
    boolean esRepetido = true;
    while (esRepetido) {
      randomLong = min + (long) (Math.random() * (max - min));
      String nroTraspaso = Long.toString(randomLong);
      Traspaso t = traspasoRepository.findByNroTraspasoAndAndEliminado(nroTraspaso, false);
      if (t == null) esRepetido = false;
    }
    return Long.toString(randomLong);
  }
}
