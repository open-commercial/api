package sic.controller;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.dto.NotaCreditoDTO;
import sic.modelo.dto.NotaDebitoDTO;
import sic.modelo.dto.RenglonesDeFacturaParaNotaCreditoDTO;
import sic.service.*;

@RestController
@RequestMapping("/api/v1")
public class NotaController {

  private final INotaService notaService;
  private final IReciboService reciboService;
  private final IEmpresaService empresaService;
  private final IClienteService clienteService;
  private final IProveedorService proveedorService;
  private final IUsuarioService usuarioService;
  private final IFacturaService facturaService;
  private final IAuthService authService;
  private final ModelMapper modelMapper;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;

  @Autowired
  public NotaController(
      INotaService notaService,
      IReciboService reciboService,
      IEmpresaService empresaService,
      IClienteService clienteService,
      IProveedorService proveedorService,
      IUsuarioService usuarioService,
      IFacturaService facturaService,
      IAuthService authService,
      ModelMapper modelMapper) {
    this.notaService = notaService;
    this.reciboService = reciboService;
    this.empresaService = empresaService;
    this.clienteService = clienteService;
    this.proveedorService = proveedorService;
    this.usuarioService = usuarioService;
    this.facturaService = facturaService;
    this.authService = authService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/notas/{idNota}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Nota getNota(@PathVariable long idNota) {
    return notaService.getNotaPorId(idNota);
  }

  @GetMapping("/notas/busqueda/criteria")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Page<Nota> buscarNotas(
    @RequestParam Long idEmpresa,
    @RequestParam(required = false) Long desde,
    @RequestParam(required = false) Long hasta,
    @RequestParam(required = false) Long idCliente,
    @RequestParam(required = false) Long idViajante,
    @RequestParam(required = false) Integer nroSerie,
    @RequestParam(required = false) Integer nroNota,
    @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
    @RequestParam(required = false) Long idUsuario,
    @RequestParam(required = false) Movimiento movimiento,
    @RequestParam(required = false) Integer pagina,
    @RequestParam(required = false) String ordenarPor,
    @RequestParam(required = false) String sentido,
    @RequestHeader("Authorization") String authorizationHeader) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (pagina == null || pagina < 0) {
      pagina = 0;
    }
    BusquedaNotaCriteria criteria =
      BusquedaNotaCriteria.builder()
        .idEmpresa(idEmpresa)
        .buscaPorFecha((desde != null) && (hasta != null))
        .fechaDesde(fechaDesde.getTime())
        .fechaHasta(fechaHasta.getTime())
        .movimiento(movimiento)
        .buscaCliente(movimiento.equals(Movimiento.VENTA) && idCliente != null)
        .idCliente(idCliente)
        .buscaViajante(movimiento.equals(Movimiento.VENTA) && idViajante != null)
        .idViajante(idViajante)
        .buscaUsuario(idUsuario != null)
        .idUsuario(idUsuario)
        .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
        .numSerie((nroSerie != null) ? nroSerie : 0)
        .numNota((nroNota != null) ? nroNota : 0)
        .buscaPorTipoComprobante(tipoDeComprobante != null)
        .tipoComprobante(tipoDeComprobante)
        .pageable(this.getPageable(pagina, ordenarPor, sentido))
        .build();
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.buscarNotas(criteria, (int) claims.get("idUsuario"));
  }

  private Pageable getPageable(int pagina, String ordenarPor, String sentido) {
    String ordenDefault = "fecha";
    if (ordenarPor == null || sentido == null) {
      return new PageRequest(pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          return new PageRequest(pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
        case "DESC":
          return new PageRequest(pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
        default:
          return new PageRequest(pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenDefault));
      }
    }
  }

  @GetMapping("/notas/tipos/empresas/{idEmpresa}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public TipoDeComprobante[] getTipoNotaCredito(@PathVariable long idEmpresa) {
    return notaService.getTiposNota(empresaService.getEmpresaPorId(idEmpresa));
  }

  @GetMapping("/notas/{idNota}/facturas")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Factura getFacturaNotaCredito(@PathVariable long idNota) {
    return notaService.getFacturaDeLaNotaCredito(idNota);
  }

  @GetMapping("/notas/debito/recibo/{idRecibo}/existe")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public boolean existeNotaDebitoRecibo(@PathVariable long idRecibo) {
    return notaService.existsNotaDebitoPorRecibo(reciboService.getById(idRecibo));
  }

  @GetMapping("/notas/tipos")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public TipoDeComprobante[] getTipoNota(
      @RequestParam long idCliente, @RequestParam long idEmpresa) {
    return notaService.getTipoNotaCliente(idCliente, idEmpresa);
  }

  @GetMapping("/notas/renglones/credito/{idNotaCredito}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public List<RenglonNotaCredito> getRenglonesDeNotaCreditoCliente(
      @PathVariable long idNotaCredito) {
    return notaService.getRenglonesDeNotaCredito(idNotaCredito);
  }

  @GetMapping("/notas/renglones/debito/{idNotaDebito}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public List<RenglonNotaDebito> getRenglonesDeNotaDebitoCliente(@PathVariable long idNotaDebito) {
    return notaService.getRenglonesDeNotaDebito(idNotaDebito);
  }

  @PostMapping("/notas/credito/empresa/{idEmpresa}/usuario/{idUsuario}/factura/{idFactura}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Nota guardarNotaCredito(
      @RequestBody NotaCreditoDTO notaCreditoDTO,
      @PathVariable long idEmpresa,
      @RequestParam Movimiento movimiento,
      @RequestParam(required = false) Long idCliente,
      @RequestParam(required = false) Long idProveedor,
      @PathVariable long idUsuario,
      @PathVariable long idFactura,
      @RequestParam boolean modificarStock) {
    NotaCredito nota = modelMapper.map(notaCreditoDTO, NotaCredito.class);
    nota.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    Factura factura = facturaService.getFacturaPorId(idFactura);
    if (movimiento == Movimiento.VENTA && idCliente != null && factura instanceof FacturaVenta) {
      nota.setCliente(clienteService.getClientePorId(idCliente));
      nota.setFacturaVenta((FacturaVenta) factura);
    } else if (movimiento == Movimiento.COMPRA
        && idProveedor != null
        && factura instanceof FacturaCompra) {
      nota.setProveedor(proveedorService.getProveedorPorId(idProveedor));
      nota.setFacturaCompra((FacturaCompra) factura);
    } else {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_movimiento_no_valido"));
    }
    nota.setMovimiento(movimiento);
    nota.setUsuario(usuarioService.getUsuarioPorId(idUsuario));
    nota.setModificaStock(modificarStock);
    return notaService.guardarNotaCredito(nota);
  }

  @PostMapping("/notas/debito/empresa/{idEmpresa}/usuario/{idUsuario}/recibo/{idRecibo}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Nota guardarNotaDebito(
      @RequestBody NotaDebitoDTO notaDebitoDTO,
      @PathVariable long idEmpresa,
      @RequestParam Movimiento movimiento,
      @RequestParam(required = false) Long idCliente,
      @RequestParam(required = false) Long idProveedor,
      @PathVariable long idUsuario,
      @PathVariable long idRecibo) {
    NotaDebito nota = modelMapper.map(notaDebitoDTO, NotaDebito.class);
    nota.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    if (movimiento == Movimiento.VENTA && idCliente != null) {
      nota.setCliente(clienteService.getClientePorId(idCliente));
    } else if (movimiento == Movimiento.COMPRA && idProveedor != null) {
      nota.setProveedor(proveedorService.getProveedorPorId(idProveedor));
    } else {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_movimiento_no_valido"));
    }
    nota.setMovimiento(movimiento);
    nota.setUsuario(usuarioService.getUsuarioPorId(idUsuario));
    nota.setRecibo(reciboService.getById(idRecibo));
    return notaService.guardarNotaDebito(nota);
  }

  @GetMapping("/notas/{idNota}/reporte")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public ResponseEntity<byte[]> getReporteNota(@PathVariable long idNota) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    Nota nota = notaService.getNotaPorId(idNota);
    String fileName =
        (nota instanceof NotaCredito)
            ? "NotaCredito.pdf"
            : (nota instanceof NotaDebito) ? "NotaDebito.pdf" : "Nota.pdf";
    headers.add("content-disposition", "inline; filename=" + fileName);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    byte[] reportePDF = notaService.getReporteNota(nota);
    return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
  }

  @PostMapping("/notas/{idNota}/autorizacion")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Nota autorizarNota(@PathVariable long idNota) {
    return notaService.autorizarNota(notaService.getNotaPorId(idNota));
  }

  @GetMapping("/notas/{idNota}/iva-neto")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getIvaNetoNota(@PathVariable long idNota) {
    return notaService.getIvaNetoNota(idNota);
  }

  @PostMapping("/notas/renglon/credito/producto")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public List<RenglonNotaCredito> calcularRenglonNotaCreditoProducto(
    @RequestBody RenglonesDeFacturaParaNotaCreditoDTO renglonesDeFacturaParaNotaCreditoDTO) {
    return notaService.calcularRenglonCredito(renglonesDeFacturaParaNotaCreditoDTO);
  }

  @GetMapping("/notas/renglon/debito/recibo/{idRecibo}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public List<RenglonNotaDebito> calcularRenglonNotaDebito(
      @PathVariable long idRecibo,
      @RequestParam BigDecimal monto,
      @RequestParam BigDecimal ivaPorcentaje) {
    return notaService.calcularRenglonDebito(idRecibo, monto, ivaPorcentaje);
  }

  @GetMapping("/notas/credito/sub-total")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal calcularSubTotalCredito(@RequestParam BigDecimal[] importe) {
    return notaService.calcularSubTotalCredito(importe);
  }

  @GetMapping("/notas/credito/descuento-neto")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal calcularDescuentoNetoCredito(
      @RequestParam BigDecimal subTotal, @RequestParam BigDecimal descuentoPorcentaje) {
    return notaService.calcularDecuentoNetoCredito(subTotal, descuentoPorcentaje);
  }

  @GetMapping("/notas/credito/recargo-neto")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal calcularRecargoNetoCredito(
      @RequestParam BigDecimal subTotal, @RequestParam BigDecimal recargoPorcentaje) {
    return notaService.calcularRecargoNetoCredito(subTotal, recargoPorcentaje);
  }

  @GetMapping("/notas/credito/iva-neto")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal calcularIVANetoCredito(
      @RequestParam TipoDeComprobante tipoDeComprobante,
      @RequestParam BigDecimal[] cantidades,
      @RequestParam BigDecimal[] ivaPorcentajeRenglones,
      @RequestParam BigDecimal[] ivaNetoRenglones,
      @RequestParam BigDecimal ivaPorcentaje,
      @RequestParam BigDecimal descuentoPorcentaje,
      @RequestParam BigDecimal recargoPorcentaje) {
    return notaService.calcularIVANetoCredito(
        tipoDeComprobante,
        cantidades,
        ivaPorcentajeRenglones,
        ivaNetoRenglones,
        ivaPorcentaje,
        descuentoPorcentaje,
        recargoPorcentaje);
  }

  @GetMapping("/notas/credito/sub-total-bruto")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal calcularSubTotalBrutoCredito(
      TipoDeComprobante tipoDeComprobante,
      BigDecimal subTotal,
      BigDecimal recargoNeto,
      BigDecimal descuentoNeto,
      BigDecimal iva105Neto,
      BigDecimal iva21Neto) {
    return notaService.calcularSubTotalBrutoCredito(
        tipoDeComprobante, subTotal, recargoNeto, descuentoNeto, iva105Neto, iva21Neto);
  }

  @GetMapping("/notas/credito/total")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal calcularTotalCredito(
      @RequestParam BigDecimal subTotalBruto,
      @RequestParam BigDecimal iva105Neto,
      @RequestParam BigDecimal iva21Neto) {
    return notaService.calcularTotalCredito(subTotalBruto, iva105Neto, iva21Neto);
  }

  @GetMapping("/notas/debito/total")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal calcularTotalDebito(
      BigDecimal subTotalBruto, BigDecimal iva21Neto, BigDecimal montoNoGravado) {
    return notaService.calcularTotalDebito(subTotalBruto, iva21Neto, montoNoGravado);
  }

  @GetMapping("/notas/total-credito/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalNotasCredito(
    @RequestParam Long idEmpresa,
    @RequestParam(required = false) Long desde,
    @RequestParam(required = false) Long hasta,
    @RequestParam(required = false) Long idCliente,
    @RequestParam(required = false) Integer nroSerie,
    @RequestParam(required = false) Integer nroNota,
    @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
    @RequestParam(required = false) Movimiento movimiento,
    @RequestParam(required = false) Long idUsuario,
    @RequestParam(required = false) Long idViajante,
    @RequestParam(required = false) Integer pagina,
    @RequestParam(required = false) String ordenarPor,
    @RequestParam(required = false) String sentido,
    @RequestHeader("Authorization") String authorizationHeader) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (pagina == null || pagina < 0) {
      pagina = 0;
    }
    BusquedaNotaCriteria criteria =
      BusquedaNotaCriteria.builder()
        .idEmpresa(idEmpresa)
        .buscaPorFecha((desde != null) && (hasta != null))
        .fechaDesde(fechaDesde.getTime())
        .fechaHasta(fechaHasta.getTime())
        .movimiento(movimiento)
        .buscaCliente(idCliente != null)
        .idCliente(idCliente)
        .buscaUsuario(idUsuario != null)
        .idUsuario(idUsuario)
        .buscaViajante(idViajante != null)
        .idViajante(idViajante)
        .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
        .numSerie((nroSerie != null) ? nroSerie : 0)
        .numNota((nroNota != null) ? nroNota : 0)
        .buscaPorTipoComprobante(tipoDeComprobante != null)
        .tipoComprobante(tipoDeComprobante)
        .pageable(this.getPageable(pagina, ordenarPor, sentido))
        .build();
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.calcularTotalCredito(criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/notas/total-debito/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalNotasDebito(
    @RequestParam Long idEmpresa,
    @RequestParam(required = false) Long desde,
    @RequestParam(required = false) Long hasta,
    @RequestParam(required = false) Long idCliente,
    @RequestParam(required = false) Integer nroSerie,
    @RequestParam(required = false) Integer nroNota,
    @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
    @RequestParam(required = false) Movimiento movimiento,
    @RequestParam(required = false) Long idUsuario,
    @RequestParam(required = false) Long idViajante,
    @RequestParam(required = false) Integer pagina,
    @RequestParam(required = false) String ordenarPor,
    @RequestParam(required = false) String sentido,
    @RequestHeader("Authorization") String authorizationHeader) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (pagina == null || pagina < 0) {
      pagina = 0;
    }
    BusquedaNotaCriteria criteria =
      BusquedaNotaCriteria.builder()
        .idEmpresa(idEmpresa)
        .buscaPorFecha((desde != null) && (hasta != null))
        .fechaDesde(fechaDesde.getTime())
        .fechaHasta(fechaHasta.getTime())
        .movimiento(movimiento)
        .buscaCliente(idCliente != null)
        .idCliente(idCliente)
        .buscaUsuario(idUsuario != null)
        .idUsuario(idUsuario)
        .buscaViajante(idViajante != null)
        .idViajante(idViajante)
        .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
        .numSerie((nroSerie != null) ? nroSerie : 0)
        .numNota((nroNota != null) ? nroNota : 0)
        .buscaPorTipoComprobante(tipoDeComprobante != null)
        .tipoComprobante(tipoDeComprobante)
        .pageable(this.getPageable(pagina, ordenarPor, sentido))
        .build();
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.calcularTotalDebito(criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/notas/total-iva-credito/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalIvaCredito(
    @RequestParam Long idEmpresa,
    @RequestParam(required = false) Long desde,
    @RequestParam(required = false) Long hasta,
    @RequestParam(required = false) Long idCliente,
    @RequestParam(required = false) Integer nroSerie,
    @RequestParam(required = false) Integer nroNota,
    @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
    @RequestParam(required = false) Movimiento movimiento,
    @RequestParam(required = false) Long idUsuario,
    @RequestParam(required = false) Long idViajante,
    @RequestParam(required = false) Integer pagina,
    @RequestParam(required = false) String ordenarPor,
    @RequestParam(required = false) String sentido,
    @RequestHeader("Authorization") String authorizationHeader) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (pagina == null || pagina < 0) {
      pagina = 0;
    }
    BusquedaNotaCriteria criteria =
      BusquedaNotaCriteria.builder()
        .idEmpresa(idEmpresa)
        .buscaPorFecha((desde != null) && (hasta != null))
        .fechaDesde(fechaDesde.getTime())
        .fechaHasta(fechaHasta.getTime())
        .movimiento(movimiento)
        .buscaCliente(idCliente != null)
        .idCliente(idCliente)
        .buscaUsuario(idUsuario != null)
        .idUsuario(idUsuario)
        .buscaViajante(idViajante != null)
        .idViajante(idViajante)
        .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
        .numSerie((nroSerie != null) ? nroSerie : 0)
        .numNota((nroNota != null) ? nroNota : 0)
        .buscaPorTipoComprobante(tipoDeComprobante != null)
        .tipoComprobante(tipoDeComprobante)
        .pageable(this.getPageable(pagina, ordenarPor, sentido))
        .build();
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.calcularTotalIVACredito(criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/notas/total-iva-debito/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalIvaDebito(
    @RequestParam Long idEmpresa,
    @RequestParam(required = false) Long desde,
    @RequestParam(required = false) Long hasta,
    @RequestParam(required = false) Long idCliente,
    @RequestParam(required = false) Integer nroSerie,
    @RequestParam(required = false) Integer nroNota,
    @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
    @RequestParam(required = false) Movimiento movimiento,
    @RequestParam(required = false) Long idUsuario,
    @RequestParam(required = false) Long idViajante,
    @RequestParam(required = false) Integer pagina,
    @RequestParam(required = false) String ordenarPor,
    @RequestParam(required = false) String sentido,
    @RequestHeader("Authorization") String authorizationHeader) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (pagina == null || pagina < 0) {
      pagina = 0;
    }
    BusquedaNotaCriteria criteria =
      BusquedaNotaCriteria.builder()
        .idEmpresa(idEmpresa)
        .buscaPorFecha((desde != null) && (hasta != null))
        .fechaDesde(fechaDesde.getTime())
        .fechaHasta(fechaHasta.getTime())
        .movimiento(movimiento)
        .buscaCliente(idCliente != null)
        .idCliente(idCliente)
        .buscaUsuario(idUsuario != null)
        .idUsuario(idUsuario)
        .buscaViajante(idViajante != null)
        .idViajante(idViajante)
        .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
        .numSerie((nroSerie != null) ? nroSerie : 0)
        .numNota((nroNota != null) ? nroNota : 0)
        .buscaPorTipoComprobante(tipoDeComprobante != null)
        .tipoComprobante(tipoDeComprobante)
        .pageable(this.getPageable(pagina, ordenarPor, sentido))
        .build();
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.calcularTotalIVADebito(criteria, (int) claims.get("idUsuario"));
  }
}
