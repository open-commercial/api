package sic.service.impl;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import sic.modelo.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sic.modelo.dto.NuevosResultadosComprobanteDTO;
import sic.modelo.Resultados;
import sic.modelo.dto.NuevoRenglonFacturaDTO;
import sic.service.*;
import sic.exception.BusinessServiceException;
import sic.repository.FacturaRepository;
import sic.repository.RenglonFacturaRepository;
import sic.util.CalculosComprobante;
import sic.util.CustomValidator;

@Service
public class FacturaServiceImpl implements IFacturaService {

  private final FacturaRepository<Factura> facturaRepository;
  private final RenglonFacturaRepository renglonFacturaRepository;
  private final IProductoService productoService;
  private final INotaService notaService;
  private static final BigDecimal IVA_21 = new BigDecimal("21");
  private static final BigDecimal IVA_105 = new BigDecimal("10.5");
  private static final BigDecimal CIEN = new BigDecimal("100");
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final MessageSource messageSource;
  private final CustomValidator customValidator;

  @Autowired
  @Lazy
  public FacturaServiceImpl(
    FacturaRepository<Factura> facturaRepository,
    RenglonFacturaRepository renglonFacturaRepository,
    IProductoService productoService,
    INotaService notaService,
    MessageSource messageSource,
    CustomValidator customValidator) {
    this.facturaRepository = facturaRepository;
    this.renglonFacturaRepository = renglonFacturaRepository;
    this.productoService = productoService;
    this.notaService = notaService;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  @Override
  public Factura getFacturaNoEliminadaPorId(long idFactura) {
    Optional<Factura> factura = facturaRepository.findById(idFactura);
    if (factura.isPresent() && !factura.get().isEliminada()) {
      return factura.get();
    } else {
      throw new EntityNotFoundException(
          messageSource.getMessage("mensaje_factura_eliminada", null, Locale.getDefault()));
    }
  }

  @Override
  public TipoDeComprobante[] getTiposDeComprobanteSegunSucursal(Sucursal sucursal) {
    if (CategoriaIVA.discriminaIVA(sucursal.getCategoriaIVA())) {
      TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[5];
      tiposPermitidos[0] = TipoDeComprobante.FACTURA_A;
      tiposPermitidos[1] = TipoDeComprobante.FACTURA_B;
      tiposPermitidos[2] = TipoDeComprobante.FACTURA_X;
      tiposPermitidos[3] = TipoDeComprobante.FACTURA_Y;
      tiposPermitidos[4] = TipoDeComprobante.PRESUPUESTO;
      return tiposPermitidos;
    } else {
      TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[4];
      tiposPermitidos[0] = TipoDeComprobante.FACTURA_C;
      tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
      tiposPermitidos[2] = TipoDeComprobante.FACTURA_Y;
      tiposPermitidos[3] = TipoDeComprobante.PRESUPUESTO;
      return tiposPermitidos;
    }
  }

  @Override
  public List<RenglonFactura> getRenglonesDeLaFactura(Long idFactura) {
    return this.getFacturaNoEliminadaPorId(idFactura).getRenglones();
  }

  @Override
  public List<RenglonFactura> getRenglonesDeLaFacturaModificadosParaCredito(Long idFactura) {
    return notaService.getRenglonesFacturaModificadosParaNotaCredito(idFactura);
  }

  @Override
  public RenglonFactura getRenglonFactura(Long idRenglonFactura) {
    return renglonFacturaRepository.findById(idRenglonFactura).orElse(null); // orElseThrow
  }

  @Override
  public Pageable getPageable(Integer pagina, String ordenarPor, String sentido) {
    if (pagina == null) pagina = 0;
    String ordenDefault = "fecha";
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(
          pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.ASC, ordenarPor));
        case "DESC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenarPor));
        default:
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenDefault));
      }
    }
  }

  @Override
  public Factura procesarFactura(Factura factura) {
    this.calcularCantidadDeArticulos(factura);
    this.validarReglasDeNegocio(factura);
    return factura;
  }

  private void calcularCantidadDeArticulos(Factura factura) {
    factura.setCantidadArticulos(BigDecimal.ZERO);
    factura
        .getRenglones()
        .forEach(
            r -> factura.setCantidadArticulos(factura.getCantidadArticulos().add(r.getCantidad())));
  }

  @Override
  public void calcularValoresFactura(Factura factura) {
    BigDecimal[] importes = new BigDecimal[factura.getRenglones().size()];
    int i = 0;
    for (RenglonFactura renglon : factura.getRenglones()) {
      importes[i] = renglon.getImporte();
      i++;
    }
    NuevosResultadosComprobanteDTO nuevosResultadosComprobante =
        new NuevosResultadosComprobanteDTO();
    nuevosResultadosComprobante.setImporte(importes);
    nuevosResultadosComprobante.setDescuentoPorcentaje(factura.getDescuentoPorcentaje());
    nuevosResultadosComprobante.setRecargoPorcentaje(factura.getRecargoPorcentaje());
    i = 0;
    if (factura.getTipoComprobante() == TipoDeComprobante.FACTURA_A
        || factura.getTipoComprobante() == TipoDeComprobante.FACTURA_B
        //    || factura.getTipoComprobante() == TipoDeComprobante.FACTURA_Y
        || factura.getTipoComprobante() == TipoDeComprobante.PRESUPUESTO) {
      BigDecimal[] ivaPorcentajes = new BigDecimal[factura.getRenglones().size()];
      BigDecimal[] ivaNetos = new BigDecimal[factura.getRenglones().size()];
      BigDecimal[] cantidades = new BigDecimal[factura.getRenglones().size()];
      for (RenglonFactura renglon : factura.getRenglones()) {
        ivaPorcentajes[i] = renglon.getIvaPorcentaje();
        ivaNetos[i] = renglon.getIvaNeto();
        cantidades[i] = renglon.getCantidad();
        i++;
      }
      nuevosResultadosComprobante.setIvaPorcentajes(ivaPorcentajes);
      nuevosResultadosComprobante.setIvaNetos(ivaNetos);
      nuevosResultadosComprobante.setCantidades(cantidades);
    }
    nuevosResultadosComprobante.setTipoDeComprobante(factura.getTipoComprobante());
    nuevosResultadosComprobante.setDescuentoPorcentaje(factura.getDescuentoPorcentaje());
    nuevosResultadosComprobante.setRecargoPorcentaje(factura.getRecargoPorcentaje());
    Resultados resultadosFactura = this.calcularResultadosFactura(nuevosResultadosComprobante);
    factura.setSubTotal(resultadosFactura.getSubTotal());
    factura.setDescuentoNeto(resultadosFactura.getDescuentoNeto());
    factura.setRecargoNeto(resultadosFactura.getRecargoNeto());
    factura.setIva21Neto(resultadosFactura.getIva21Neto());
    factura.setIva105Neto(resultadosFactura.getIva105Neto());
    factura.setSubTotalBruto(resultadosFactura.getSubTotalBruto());
    factura.setTotal(resultadosFactura.getTotal());
  }

  @Override
  public Map<Long, BigDecimal> getIdsProductosYCantidades(Factura factura) {
    Map<Long, BigDecimal> idsYCantidades = new HashMap<>();
    factura.getRenglones().forEach(r -> idsYCantidades.put(r.getIdProductoItem(), r.getCantidad()));
    return idsYCantidades;
  }

  @Override
  public void validarReglasDeNegocio(Factura factura) {
    // Requeridos
    if (factura instanceof FacturaCompra && factura.getFecha().isAfter(LocalDateTime.now())) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_factura_compra_fecha_incorrecta", null, Locale.getDefault()));
    }
    if (factura instanceof FacturaVenta) {
      FacturaVenta facturaVenta = (FacturaVenta) factura;
      if (facturaVenta.getCae() != 0L) {
        throw new BusinessServiceException(
            messageSource.getMessage("mensaje_factura_venta_cae", null, Locale.getDefault()));
      }
    }
  }

  @Override
  public BigDecimal calcularIVANetoRenglon(
      Movimiento movimiento,
      TipoDeComprobante tipo,
      Producto producto,
      BigDecimal bonificacionPorcentaje) {
    BigDecimal resultado = BigDecimal.ZERO;
    if (movimiento == Movimiento.COMPRA) {
      if (tipo == TipoDeComprobante.FACTURA_A || tipo == TipoDeComprobante.FACTURA_B) {
        resultado =
            producto
                .getPrecioProducto()
                .getPrecioCosto()
                .multiply(
                    BigDecimal.ONE
                        .subtract(bonificacionPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP))
                        .multiply(
                            producto.getPrecioProducto().getIvaPorcentaje().divide(CIEN, 15, RoundingMode.HALF_UP)));
      }
    } else if (movimiento == Movimiento.VENTA
        && (tipo == TipoDeComprobante.FACTURA_A
            || tipo == TipoDeComprobante.FACTURA_B
            || tipo == TipoDeComprobante.PRESUPUESTO)) {
      resultado =
          producto
              .getPrecioProducto()
              .getPrecioVentaPublico()
              .multiply(
                  BigDecimal.ONE
                      .subtract(bonificacionPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP))
                      .multiply(
                          producto.getPrecioProducto().getIvaPorcentaje().divide(CIEN, 15, RoundingMode.HALF_UP)));
    }
    return resultado;
  }

  @Override
  public BigDecimal calcularIvaNetoFactura(
      TipoDeComprobante tipo,
      BigDecimal[] cantidades,
      BigDecimal[] ivaPorcentajeRenglones,
      BigDecimal[] ivaNetoRenglones,
      BigDecimal ivaPorcentaje,
      BigDecimal descuentoPorcentaje,
      BigDecimal recargoPorcentaje) {
    BigDecimal resultado = BigDecimal.ZERO;
    int indice = cantidades.length;
    for (int i = 0; i < indice; i++) {
      if (ivaPorcentajeRenglones[i].compareTo(ivaPorcentaje) == 0) {
        if (tipo == TipoDeComprobante.FACTURA_A
            || tipo == TipoDeComprobante.FACTURA_B
            || tipo == TipoDeComprobante.FACTURA_C
            //    || tipo == TipoDeComprobante.FACTURA_Y
            || tipo == TipoDeComprobante.PRESUPUESTO) {
          resultado =
              resultado.add(
                  cantidades[i].multiply(
                      ivaNetoRenglones[i]
                          .subtract(
                              ivaNetoRenglones[i].multiply(
                                  descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
                          .add(
                              ivaNetoRenglones[i].multiply(
                                  recargoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))));
        } else {
          resultado = resultado.add(cantidades[i].multiply(ivaNetoRenglones[i]));
        }
      }
    }
    return resultado;
  }

  @Override
  public BigDecimal calcularPrecioUnitario(
      Movimiento movimiento, TipoDeComprobante tipoDeComprobante, Producto producto) {
    BigDecimal ivaResultado;
    BigDecimal resultado = BigDecimal.ZERO;
    if (movimiento == Movimiento.COMPRA) {
      if (tipoDeComprobante.equals(TipoDeComprobante.FACTURA_A)
          || tipoDeComprobante.equals(TipoDeComprobante.FACTURA_X)) {
        resultado = producto.getPrecioProducto().getPrecioCosto();
      } else {
        ivaResultado =
            producto
                .getPrecioProducto()
                .getPrecioCosto()
                .multiply(producto.getPrecioProducto().getIvaPorcentaje())
                .divide(CIEN, 15, RoundingMode.HALF_UP);
        resultado = producto.getPrecioProducto().getPrecioCosto().add(ivaResultado);
      }
    }
    if (movimiento == Movimiento.VENTA) {
      resultado = switch (tipoDeComprobante) {
        case FACTURA_A, FACTURA_X -> producto.getPrecioProducto().getPrecioVentaPublico();
        default -> producto.getPrecioProducto().getPrecioLista();
      };
    }
    if (movimiento == Movimiento.PEDIDO) {
      resultado = producto.getPrecioProducto().getPrecioLista();
    }
    return resultado;
  }

  @Override
  public List<RenglonFactura> calcularRenglones(
      TipoDeComprobante tipoDeComprobante,
      Movimiento movimiento,
      List<NuevoRenglonFacturaDTO> nuevosRenglonesFacturaDTO) {
    nuevosRenglonesFacturaDTO.forEach(customValidator::validar);
    List<RenglonFactura> renglones = new ArrayList<>();
    nuevosRenglonesFacturaDTO.forEach(
        nuevoRenglonFacturaDTO -> {
          if (movimiento.equals(Movimiento.VENTA)) {
            nuevoRenglonFacturaDTO.setRenglonMarcado(
                this.marcarRenglonParaAplicarBonificacion(
                    nuevoRenglonFacturaDTO.getIdProducto(), nuevoRenglonFacturaDTO.getCantidad()));
          }
          renglones.add(
              this.calcularRenglon(tipoDeComprobante, movimiento, nuevoRenglonFacturaDTO));
        });
    return renglones;
  }

  @Override
  public RenglonFactura calcularRenglon(
      TipoDeComprobante tipoDeComprobante,
      Movimiento movimiento,
      NuevoRenglonFacturaDTO nuevoRenglonFacturaDTO) {
    customValidator.validar(nuevoRenglonFacturaDTO);
    Producto producto = productoService.getProductoNoEliminadoPorId(nuevoRenglonFacturaDTO.getIdProducto());
    RenglonFactura nuevoRenglon = new RenglonFactura();
    nuevoRenglon.setIdProductoItem(producto.getIdProducto());
    nuevoRenglon.setCodigoItem(producto.getCodigo());
    nuevoRenglon.setDescripcionItem(producto.getDescripcion());
    nuevoRenglon.setMedidaItem(producto.getMedida().getNombre());
    nuevoRenglon.setCantidad(nuevoRenglonFacturaDTO.getCantidad());
    nuevoRenglon.setPrecioUnitario(
        this.calcularPrecioUnitario(movimiento, tipoDeComprobante, producto));
    if (movimiento.equals(Movimiento.VENTA) || movimiento.equals(Movimiento.PEDIDO)) {
      this.aplicarBonificacion(nuevoRenglon, producto, nuevoRenglonFacturaDTO.isRenglonMarcado());
    } else {
      nuevoRenglon.setBonificacionPorcentaje(
          nuevoRenglonFacturaDTO.getBonificacion() != null
              ? nuevoRenglonFacturaDTO.getBonificacion()
              : BigDecimal.ZERO);
      nuevoRenglon.setBonificacionNeta(
          CalculosComprobante.calcularProporcion(
              nuevoRenglon.getPrecioUnitario(), nuevoRenglonFacturaDTO.getBonificacion()));
    }
    nuevoRenglon.setIvaPorcentaje(producto.getPrecioProducto().getIvaPorcentaje());
    nuevoRenglon.setIvaNeto(
        this.calcularIVANetoRenglon(
            movimiento, tipoDeComprobante, producto, nuevoRenglon.getBonificacionPorcentaje()));
    nuevoRenglon.setGananciaPorcentaje(producto.getPrecioProducto().getGananciaPorcentaje());
    nuevoRenglon.setGananciaNeto(producto.getPrecioProducto().getGananciaNeto());
    nuevoRenglon.setImporteAnterior(
        CalculosComprobante.calcularImporte(
            nuevoRenglon.getCantidad(), producto.getPrecioProducto().getPrecioLista(), BigDecimal.ZERO));
    nuevoRenglon.setImporte(
        CalculosComprobante.calcularImporte(
            nuevoRenglonFacturaDTO.getCantidad(),
            nuevoRenglon.getPrecioUnitario(),
            nuevoRenglon.getBonificacionNeta()));
    return nuevoRenglon;
  }

  @Override
  public Resultados calcularResultadosFactura(
      NuevosResultadosComprobanteDTO nuevosResultadosComprobante) {
    Resultados resultados = new Resultados();
    // subTotal
    resultados.setSubTotal(
        CalculosComprobante.calcularSubTotal(nuevosResultadosComprobante.getImporte()));
    // Descuentos y Recargos
    resultados.setDescuentoNeto(
        CalculosComprobante.calcularProporcion(
            resultados.getSubTotal(), nuevosResultadosComprobante.getDescuentoPorcentaje()));
    resultados.setRecargoNeto(
        CalculosComprobante.calcularProporcion(
            resultados.getSubTotal(), nuevosResultadosComprobante.getRecargoPorcentaje()));
    // IVA
    if (nuevosResultadosComprobante.getTipoDeComprobante() == TipoDeComprobante.FACTURA_A
        || nuevosResultadosComprobante.getTipoDeComprobante() == TipoDeComprobante.FACTURA_B
        // || nuevosResultadosComprobante.getTipoDeComprobante() == TipoDeComprobante.FACTURA_Y
        || nuevosResultadosComprobante.getTipoDeComprobante() == TipoDeComprobante.PRESUPUESTO) {
      int longitudDeArraysValido = nuevosResultadosComprobante.getCantidades().length;
      if (nuevosResultadosComprobante.getIvaNetos().length != longitudDeArraysValido
          || nuevosResultadosComprobante.getIvaPorcentajes().length != longitudDeArraysValido) {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_factura_renglones_parametros_no_validos", null, Locale.getDefault()));
      }
      resultados.setIva21Neto(
          this.calcularIvaNetoFactura(
              nuevosResultadosComprobante.getTipoDeComprobante(),
              nuevosResultadosComprobante.getCantidades(),
              nuevosResultadosComprobante.getIvaPorcentajes(),
              nuevosResultadosComprobante.getIvaNetos(),
              IVA_21,
              nuevosResultadosComprobante.getDescuentoPorcentaje(),
              nuevosResultadosComprobante.getRecargoPorcentaje()));
      resultados.setIva105Neto(
          this.calcularIvaNetoFactura(
              nuevosResultadosComprobante.getTipoDeComprobante(),
              nuevosResultadosComprobante.getCantidades(),
              nuevosResultadosComprobante.getIvaPorcentajes(),
              nuevosResultadosComprobante.getIvaNetos(),
              IVA_105,
              nuevosResultadosComprobante.getDescuentoPorcentaje(),
              nuevosResultadosComprobante.getRecargoPorcentaje()));
    }
    if (nuevosResultadosComprobante.getTipoDeComprobante() == TipoDeComprobante.FACTURA_X
        || nuevosResultadosComprobante.getTipoDeComprobante() == TipoDeComprobante.FACTURA_C) {
      resultados.setIva21Neto(BigDecimal.ZERO);
      resultados.setIva105Neto(BigDecimal.ZERO);
    }
    // SubTotalBruto
    resultados.setSubTotalBruto(
        CalculosComprobante.calcularSubTotalBruto(
            (nuevosResultadosComprobante.getTipoDeComprobante() == TipoDeComprobante.FACTURA_B
                || nuevosResultadosComprobante.getTipoDeComprobante()
                    == TipoDeComprobante.PRESUPUESTO),
            resultados.getSubTotal(),
            resultados.getRecargoNeto(),
            resultados.getDescuentoNeto(),
            resultados.getIva105Neto(),
            resultados.getIva21Neto()));
    // Total
    resultados.setTotal(
        CalculosComprobante.calcularTotal(
            resultados.getSubTotalBruto(), resultados.getIva105Neto(), resultados.getIva21Neto()));
    return resultados;
  }

  @Override
  public void aplicarBonificacion(
          RenglonFactura nuevoRenglon, Producto producto, boolean aplicaBonificacion) {
    if (producto.getPrecioProducto().isOferta() && aplicaBonificacion) {
      nuevoRenglon.setBonificacionPorcentaje(producto.getPrecioProducto().getPorcentajeBonificacionOferta());
      nuevoRenglon.setBonificacionNeta(
              CalculosComprobante.calcularProporcion(
                      nuevoRenglon.getPrecioUnitario(), producto.getPrecioProducto().getPorcentajeBonificacionOferta()));
    } else if (aplicaBonificacion && producto.getPrecioProducto().getPorcentajeBonificacionPrecio() != null) {
      nuevoRenglon.setBonificacionPorcentaje(producto.getPrecioProducto().getPorcentajeBonificacionPrecio());
      nuevoRenglon.setBonificacionNeta(
              CalculosComprobante.calcularProporcion(
                      nuevoRenglon.getPrecioUnitario(), producto.getPrecioProducto().getPorcentajeBonificacionPrecio()));
    } else {
      nuevoRenglon.setBonificacionPorcentaje(BigDecimal.ZERO);
      nuevoRenglon.setBonificacionNeta(BigDecimal.ZERO);
    }
  }

  @Override
  public boolean marcarRenglonParaAplicarBonificacion(long idProducto, BigDecimal cantidad) {
    Producto producto = productoService.getProductoNoEliminadoPorId(idProducto);
    return cantidad.compareTo(producto.getCantidadProducto().getBulto()) >= 0;
  }
}
