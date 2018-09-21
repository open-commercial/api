package sic.controller;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import sic.service.IEmpresaService;
import sic.service.INotaService;
import sic.service.IReciboService;

@RestController
@RequestMapping("/api/v1")
public class NotaController {

    private final INotaService notaService;
    private final IReciboService reciboService;
    private final IEmpresaService empresaService;
    private final ModelMapper modelMapper;
    private static final int TAMANIO_PAGINA_DEFAULT = 50;

    @Value("${SIC_JWT_KEY}")
    private String secretkey;

    @Autowired
    public NotaController(INotaService notaService, IReciboService reciboService,
                          IEmpresaService empresaService, ModelMapper modelMapper) {
        this.notaService = notaService;
        this.reciboService = reciboService;
        this.empresaService = empresaService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/notas/{idNota}")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public Nota getNota(@PathVariable long idNota) {
        return notaService.getNotaPorId(idNota);
    }

  @GetMapping("/notas/ventas/busqueda/criteria")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Page<Nota> buscarNotasVenta(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) Long idCliente,
      @RequestParam(required = false) Integer nroSerie,
      @RequestParam(required = false) Integer nroNota,
      @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
      @RequestParam(required = false) Long idUsuario,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) Integer tamanio,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestHeader("Authorization") String token) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (tamanio == null || tamanio <= 0) {
      tamanio = TAMANIO_PAGINA_DEFAULT;
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
            .buscaVentas(true)
            .buscaCliente(idCliente != null)
            .idCliente(idCliente)
            .buscaUsuario(idUsuario != null)
            .idUsuario(idUsuario)
            .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
            .numSerie((nroSerie != null) ? nroSerie : 0)
            .numNota((nroNota != null) ? nroNota : 0)
            .buscaPorTipoComprobante(tipoDeComprobante != null)
            .tipoComprobante(tipoDeComprobante)
            .pageable(this.getPageable(pagina, tamanio, ordenarPor, sentido))
            .build();
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
    return notaService.buscarNotas(criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/notas/compras/busqueda/criteria")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR
  })
  public Page<Nota> buscarNotasCompra(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) Long idProveedor,
      @RequestParam(required = false) Integer nroSerie,
      @RequestParam(required = false) Integer nroNota,
      @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
      @RequestParam(required = false) Long idUsuario,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) Integer tamanio,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestHeader("Authorization") String token) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (tamanio == null || tamanio <= 0) {
      tamanio = TAMANIO_PAGINA_DEFAULT;
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
            .buscaCompras(true)
            .buscaProveedor(idProveedor != null)
            .idCliente(idProveedor)
            .buscaUsuario(idUsuario != null)
            .idUsuario(idUsuario)
            .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
            .numSerie((nroSerie != null) ? nroSerie : 0)
            .numNota((nroNota != null) ? nroNota : 0)
            .buscaPorTipoComprobante(tipoDeComprobante != null)
            .tipoComprobante(tipoDeComprobante)
            .pageable(this.getPageable(pagina, tamanio, ordenarPor, sentido))
            .build();
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
    return notaService.buscarNotas(criteria, (int) claims.get("idUsuario"));
  }

  private Pageable getPageable(int pagina, int tamanio, String ordenarPor, String sentido) {
    String ordenDefault = "fecha";
    if (ordenarPor == null || sentido == null) {
      return new PageRequest(pagina, tamanio, new Sort(Sort.Direction.DESC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          return new PageRequest(pagina, tamanio, new Sort(Sort.Direction.ASC, ordenarPor));
        case "DESC":
          return new PageRequest(pagina, tamanio, new Sort(Sort.Direction.DESC, ordenarPor));
        default:
          return new PageRequest(pagina, tamanio, new Sort(Sort.Direction.DESC, ordenDefault));
      }
    }
  }

  @GetMapping("/notas/tipos/empresas/{idEmpresa}")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public TipoDeComprobante[] getTipoNotaCredito(@PathVariable long idEmpresa) {
    return notaService.getTiposNota(empresaService.getEmpresaPorId(idEmpresa));
  }

    @GetMapping("/notas/{idNota}/facturas")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public Factura getFacturaNotaCredito(@PathVariable long idNota) {
        return notaService.getFacturaNotaCredito(idNota);
    }

    @GetMapping("/notas/debito/recibo/{idRecibo}/existe")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public boolean existeNotaDebitoRecibo(@PathVariable long idRecibo) {
        return notaService.existeNotaDebitoPorRecibo(reciboService.getById(idRecibo));
    }

    @GetMapping("/notas/tipos")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public TipoDeComprobante[] getTipoNota(@RequestParam long idCliente,
                                           @RequestParam long idEmpresa) {
        return notaService.getTipoNotaCliente(idCliente, idEmpresa);
    }

    @GetMapping("/notas/renglones/credito/{idNotaCredito}")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public List<RenglonNotaCredito> getRenglonesDeNotaCreditoCliente(@PathVariable long idNotaCredito) {
        return notaService.getRenglonesDeNotaCredito(idNotaCredito);
    }

    @GetMapping("/notas/renglones/debito/{idNotaDebito}")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public List<RenglonNotaDebito> getRenglonesDeNotaDebitoCliente(@PathVariable long idNotaDebito) {
        return notaService.getRenglonesDeNotaDebito(idNotaDebito);
    }

  @PostMapping(
      "/notas/credito/empresa/{idEmpresa}/cliente/{idCliente}/usuario/{idUsuario}/factura/{idFactura}")
  @ResponseStatus(HttpStatus.CREATED)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Nota guardarNotaCreditoCliente(
      @RequestBody NotaCreditoDTO notaCreditoDTO,
      @PathVariable long idEmpresa,
      @PathVariable long idCliente,
      @PathVariable long idUsuario,
      @PathVariable long idFactura,
      @RequestParam boolean modificarStock) {
    NotaCredito nota = modelMapper.map(notaCreditoDTO, NotaCredito.class);
    return notaService.guardarNotaCliente(
        nota, idEmpresa, idCliente, idUsuario, null, idFactura, modificarStock);
  }

  @PostMapping(
      "/notas/debito/empresa/{idEmpresa}/cliente/{idCliente}/usuario/{idUsuario}/recibo/{idRecibo}")
  @ResponseStatus(HttpStatus.CREATED)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Nota guardarNotaDebitoCliente(
      @RequestBody NotaDebitoDTO notaDebitoDTO,
      @PathVariable long idEmpresa,
      @PathVariable long idCliente,
      @PathVariable long idUsuario,
      @PathVariable long idRecibo) {
    NotaDebito nota = modelMapper.map(notaDebitoDTO, NotaDebito.class);
    return notaService.guardarNotaCliente(
        nota, idEmpresa, idCliente, idUsuario, idRecibo, null, false);
  }

  @PostMapping(
      "/notas/credito/empresa/{idEmpresa}/proveedor/{idProveedor}/usuario/{idUsuario}/factura/{idFactura}")
  @ResponseStatus(HttpStatus.CREATED)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Nota guardarNotaCreditoProveedor(
      @RequestBody NotaCreditoDTO notaCreditoDTO,
      @PathVariable long idEmpresa,
      @PathVariable long idProveedor,
      @PathVariable long idUsuario,
      @PathVariable long idFactura,
      @RequestParam boolean modificarStock) {
    NotaCredito nota = modelMapper.map(notaCreditoDTO, NotaCredito.class);
    return notaService.guardarNotaProveedor(
        nota, idEmpresa, idProveedor, idUsuario, null, idFactura, modificarStock);
  }

  @PostMapping(
      "/notas/debito/empresa/{idEmpresa}/proveedor/{idProveedor}/usuario/{idUsuario}/recibo/{idRecibo}")
  @ResponseStatus(HttpStatus.CREATED)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Nota guardarNotaDebitoProveedor(
      @RequestBody NotaDebitoDTO notaDebitoDTO,
      @PathVariable long idEmpresa,
      @PathVariable long idProveedor,
      @PathVariable long idUsuario,
      @PathVariable long idRecibo) {
    NotaDebito nota = modelMapper.map(notaDebitoDTO, NotaDebito.class);
    return notaService.guardarNotaProveedor(
        nota, idEmpresa, idProveedor, idUsuario, idRecibo, null, false);
  }

    @GetMapping("/notas/{idNota}/reporte")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public ResponseEntity<byte[]> getReporteNota(@PathVariable long idNota) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        Nota nota = notaService.getNotaPorId(idNota);
        String fileName = (nota instanceof NotaCredito) ? "NotaCredito.pdf" : (nota instanceof NotaDebito) ? "NotaDebito.pdf" : "Nota.pdf";
        headers.add("content-disposition", "inline; filename=" + fileName);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        byte[] reportePDF = notaService.getReporteNota(nota);
        return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
    }

    @PostMapping("/notas/{idNota}/autorizacion")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public Nota autorizarNota(@PathVariable long idNota) {
        return notaService.autorizarNota(notaService.getNotaPorId(idNota));
    }

    @DeleteMapping("/notas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
    public void eliminarNota(@RequestParam long[] idsNota) {
        notaService.eliminarNota(idsNota);
    }

    @GetMapping("/notas/{idNota}/iva-neto")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public BigDecimal getIvaNetoNota(@PathVariable long idNota) {
        return notaService.getIvaNetoNota(idNota);
    }

    @GetMapping("/notas/renglon/credito/producto")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public List<RenglonNotaCredito> calcularRenglonNotaCreditoProducto(@RequestParam TipoDeComprobante tipoDeComprobante,
                                                                       @RequestParam BigDecimal[] cantidad,
                                                                       @RequestParam long[] idRenglonFactura) {
        return notaService.calcularRenglonCredito(tipoDeComprobante, cantidad, idRenglonFactura);
    }

    @GetMapping("/notas/renglon/debito/recibo/{idRecibo}")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public List<RenglonNotaDebito> calcularRenglonNotaDebito(@PathVariable long idRecibo,
                                                             @RequestParam BigDecimal monto,
                                                             @RequestParam BigDecimal ivaPorcentaje) {
        return notaService.calcularRenglonDebito(idRecibo, monto, ivaPorcentaje);
    }

    @GetMapping("/notas/credito/sub-total")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public BigDecimal calcularSubTotalCredito(@RequestParam BigDecimal[] importe) {
        return notaService.calcularSubTotalCredito(importe);
    }

    @GetMapping("/notas/credito/descuento-neto")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public BigDecimal calcularDescuentoNetoCredito(@RequestParam BigDecimal subTotal,
                                                   @RequestParam BigDecimal descuentoPorcentaje) {
        return notaService.calcularDecuentoNetoCredito(subTotal, descuentoPorcentaje);
    }

    @GetMapping("/notas/credito/recargo-neto")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public BigDecimal calcularRecargoNetoCredito(@RequestParam BigDecimal subTotal,
                                                 @RequestParam BigDecimal recargoPorcentaje) {
        return notaService.calcularRecargoNetoCredito(subTotal, recargoPorcentaje);
    }

    @GetMapping("/notas/credito/iva-neto")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public BigDecimal calcularIVANetoCredito(@RequestParam TipoDeComprobante tipoDeComprobante,
                                             @RequestParam BigDecimal[] cantidades,
                                             @RequestParam BigDecimal[] ivaPorcentajeRenglones,
                                             @RequestParam BigDecimal[] ivaNetoRenglones,
                                             @RequestParam BigDecimal ivaPorcentaje,
                                             @RequestParam BigDecimal descuentoPorcentaje,
                                             @RequestParam BigDecimal recargoPorcentaje){
        return notaService.calcularIVANetoCredito(tipoDeComprobante, cantidades, ivaPorcentajeRenglones, ivaNetoRenglones, ivaPorcentaje, descuentoPorcentaje, recargoPorcentaje);
    }

    @GetMapping("/notas/credito/sub-total-bruto")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public BigDecimal calcularSubTotalBrutoCredito(TipoDeComprobante tipoDeComprobante,
                                                   BigDecimal subTotal,
                                                   BigDecimal recargoNeto,
                                                   BigDecimal descuentoNeto,
                                                   BigDecimal iva105Neto,
                                                   BigDecimal iva21Neto) {
        return notaService.calcularSubTotalBrutoCredito(tipoDeComprobante, subTotal, recargoNeto, descuentoNeto, iva105Neto, iva21Neto);
    }

    @GetMapping("/notas/credito/total")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public BigDecimal calcularTotalCredito(@RequestParam BigDecimal subTotalBruto,
                                           @RequestParam BigDecimal iva105Neto,
                                           @RequestParam BigDecimal iva21Neto) {
        return notaService.calcularTotalCredito(subTotalBruto, iva105Neto, iva21Neto);
    }

    @GetMapping("/notas/debito/total")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public BigDecimal calcularTotalDebito(BigDecimal subTotalBruto,
                                          BigDecimal iva21Neto,
                                          BigDecimal montoNoGravado) {
        return notaService.calcularTotalDebito(subTotalBruto, iva21Neto, montoNoGravado);
    }

  @GetMapping("/notas/ventas/total-credito/criteria")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalNotasVentaCredito(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) Long idCliente,
      @RequestParam(required = false) Integer nroSerie,
      @RequestParam(required = false) Integer nroNota,
      @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
      @RequestParam(required = false) Long idUsuario,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) Integer tamanio,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestHeader("Authorization") String token) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (tamanio == null || tamanio <= 0) {
      tamanio = TAMANIO_PAGINA_DEFAULT;
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
            .buscaVentas(true)
            .buscaCliente(idCliente != null)
            .idCliente(idCliente)
            .buscaUsuario(idUsuario != null)
            .idUsuario(idUsuario)
            .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
            .numSerie((nroSerie != null) ? nroSerie : 0)
            .numNota((nroNota != null) ? nroNota : 0)
            .buscaPorTipoComprobante(tipoDeComprobante != null)
            .tipoComprobante(tipoDeComprobante)
            .pageable(this.getPageable(pagina, tamanio, ordenarPor, sentido))
            .build();
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
    return notaService.calcularTotalCredito(criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/notas/ventas/total-debito/criteria")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalNotasVentaDebito(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) Long idCliente,
      @RequestParam(required = false) Integer nroSerie,
      @RequestParam(required = false) Integer nroNota,
      @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
      @RequestParam(required = false) Long idUsuario,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) Integer tamanio,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestHeader("Authorization") String token) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (tamanio == null || tamanio <= 0) {
      tamanio = TAMANIO_PAGINA_DEFAULT;
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
            .buscaVentas(true)
            .buscaCliente(idCliente != null)
            .idCliente(idCliente)
            .buscaUsuario(idUsuario != null)
            .idUsuario(idUsuario)
            .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
            .numSerie((nroSerie != null) ? nroSerie : 0)
            .numNota((nroNota != null) ? nroNota : 0)
            .buscaPorTipoComprobante(tipoDeComprobante != null)
            .tipoComprobante(tipoDeComprobante)
            .pageable(this.getPageable(pagina, tamanio, ordenarPor, sentido))
            .build();
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
    return notaService.calcularTotalDebito(criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/notas/ventas/total-iva-credito/criteria")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalIvaCreditoVentas(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) Long idCliente,
      @RequestParam(required = false) Integer nroSerie,
      @RequestParam(required = false) Integer nroNota,
      @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
      @RequestParam(required = false) Long idUsuario,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) Integer tamanio,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestHeader("Authorization") String token) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (tamanio == null || tamanio <= 0) {
      tamanio = TAMANIO_PAGINA_DEFAULT;
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
            .buscaVentas(true)
            .buscaCliente(idCliente != null)
            .idCliente(idCliente)
            .buscaUsuario(idUsuario != null)
            .idUsuario(idUsuario)
            .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
            .numSerie((nroSerie != null) ? nroSerie : 0)
            .numNota((nroNota != null) ? nroNota : 0)
            .buscaPorTipoComprobante(tipoDeComprobante != null)
            .tipoComprobante(tipoDeComprobante)
            .pageable(this.getPageable(pagina, tamanio, ordenarPor, sentido))
            .build();
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
    return notaService.calcularTotalIVACredito(criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/notas/ventas/total-iva-debito/criteria")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalIvaDebitoVentas(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) Long idCliente,
      @RequestParam(required = false) Integer nroSerie,
      @RequestParam(required = false) Integer nroNota,
      @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
      @RequestParam(required = false) Long idUsuario,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) Integer tamanio,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestHeader("Authorization") String token) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (tamanio == null || tamanio <= 0) {
      tamanio = TAMANIO_PAGINA_DEFAULT;
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
            .buscaVentas(true)
            .buscaCliente(idCliente != null)
            .idCliente(idCliente)
            .buscaUsuario(idUsuario != null)
            .idUsuario(idUsuario)
            .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
            .numSerie((nroSerie != null) ? nroSerie : 0)
            .numNota((nroNota != null) ? nroNota : 0)
            .buscaPorTipoComprobante(tipoDeComprobante != null)
            .tipoComprobante(tipoDeComprobante)
            .pageable(this.getPageable(pagina, tamanio, ordenarPor, sentido))
            .build();
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
    return notaService.calcularTotalIVADebito(criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/notas/compras/total-credito/criteria")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalCompraCredito(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) Long idProveedor,
      @RequestParam(required = false) Integer nroSerie,
      @RequestParam(required = false) Integer nroNota,
      @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
      @RequestParam(required = false) Long idUsuario,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) Integer tamanio,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestHeader("Authorization") String token) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (tamanio == null || tamanio <= 0) {
      tamanio = TAMANIO_PAGINA_DEFAULT;
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
            .buscaCompras(true)
            .buscaProveedor(idProveedor != null)
            .idCliente(idProveedor)
            .buscaUsuario(idUsuario != null)
            .idUsuario(idUsuario)
            .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
            .numSerie((nroSerie != null) ? nroSerie : 0)
            .numNota((nroNota != null) ? nroNota : 0)
            .buscaPorTipoComprobante(tipoDeComprobante != null)
            .tipoComprobante(tipoDeComprobante)
            .pageable(this.getPageable(pagina, tamanio, ordenarPor, sentido))
            .build();
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
    return notaService.calcularTotalCredito(criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/notas/compras/total-debito/criteria")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalCompraDebito(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) Long idProveedor,
      @RequestParam(required = false) Integer nroSerie,
      @RequestParam(required = false) Integer nroNota,
      @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
      @RequestParam(required = false) Long idUsuario,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) Integer tamanio,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestHeader("Authorization") String token) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (tamanio == null || tamanio <= 0) {
      tamanio = TAMANIO_PAGINA_DEFAULT;
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
            .buscaCompras(true)
            .buscaProveedor(idProveedor != null)
            .idCliente(idProveedor)
            .buscaUsuario(idUsuario != null)
            .idUsuario(idUsuario)
            .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
            .numSerie((nroSerie != null) ? nroSerie : 0)
            .numNota((nroNota != null) ? nroNota : 0)
            .buscaPorTipoComprobante(tipoDeComprobante != null)
            .tipoComprobante(tipoDeComprobante)
            .pageable(this.getPageable(pagina, tamanio, ordenarPor, sentido))
            .build();
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
    return notaService.calcularTotalDebito(criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/notas/compras/total-iva-credito/criteria")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalIvaCreditoCompras(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) Long idProveedor,
      @RequestParam(required = false) Integer nroSerie,
      @RequestParam(required = false) Integer nroNota,
      @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
      @RequestParam(required = false) Long idUsuario,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) Integer tamanio,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestHeader("Authorization") String token) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (tamanio == null || tamanio <= 0) {
      tamanio = TAMANIO_PAGINA_DEFAULT;
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
            .buscaCompras(true)
            .buscaProveedor(idProveedor != null)
            .idCliente(idProveedor)
            .buscaUsuario(idUsuario != null)
            .idUsuario(idUsuario)
            .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
            .numSerie((nroSerie != null) ? nroSerie : 0)
            .numNota((nroNota != null) ? nroNota : 0)
            .buscaPorTipoComprobante(tipoDeComprobante != null)
            .tipoComprobante(tipoDeComprobante)
            .pageable(this.getPageable(pagina, tamanio, ordenarPor, sentido))
            .build();
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
    return notaService.calcularTotalIVACredito(criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/notas/compras/total-iva-debito/criteria")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalIvaDebitoCompras(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) Long idProveedor,
      @RequestParam(required = false) Integer nroSerie,
      @RequestParam(required = false) Integer nroNota,
      @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
      @RequestParam(required = false) Long idUsuario,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) Integer tamanio,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestHeader("Authorization") String token) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (tamanio == null || tamanio <= 0) {
      tamanio = TAMANIO_PAGINA_DEFAULT;
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
            .buscaCompras(true)
            .buscaProveedor(idProveedor != null)
            .idCliente(idProveedor)
            .buscaUsuario(idUsuario != null)
            .idUsuario(idUsuario)
            .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
            .numSerie((nroSerie != null) ? nroSerie : 0)
            .numNota((nroNota != null) ? nroNota : 0)
            .buscaPorTipoComprobante(tipoDeComprobante != null)
            .tipoComprobante(tipoDeComprobante)
            .pageable(this.getPageable(pagina, tamanio, ordenarPor, sentido))
            .build();
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
    return notaService.calcularTotalIVADebito(criteria, (int) claims.get("idUsuario"));
  }
}
