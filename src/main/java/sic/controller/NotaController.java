package sic.controller;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
import sic.service.INotaService;
import sic.service.IReciboService;

@RestController
@RequestMapping("/api/v1")
public class NotaController {
    
    private final INotaService notaService;
    private final IReciboService reciboService;
    private static final int TAMANIO_PAGINA_DEFAULT = 50;

    @Value("${SIC_JWT_KEY}")
    private String secretkey;

    @Autowired
    public NotaController(INotaService notaService, IReciboService reciboService) {
        this.notaService = notaService;
        this.reciboService = reciboService;
    }

    @GetMapping("/notas/{idNota}")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public Nota getNota(@PathVariable long idNota) {
        return notaService.getNotaPorId(idNota);
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
    
    @GetMapping("/notas/renglones/credito/clientes/{idNotaCredito}")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public List<RenglonNotaCredito> getRenglonesDeNotaCreditoCliente(@PathVariable long idNotaCredito) {
        return notaService.getRenglonesDeNotaCreditoCliente(idNotaCredito);
    }
    
    @GetMapping("/notas/renglones/debito/clientes/{idNotaDebito}")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public List<RenglonNotaDebito> getRenglonesDeNotaDebitoCliente(@PathVariable long idNotaDebito) {
        return notaService.getRenglonesDeNotaDebitoCliente(idNotaDebito);
    }
    
    @GetMapping("/notas/renglones/credito/proveedores/{idNotaCredito}")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public List<RenglonNotaCredito> getRenglonesDeNotaCreditoProveedor(@PathVariable long idNotaCredito) {
        return notaService.getRenglonesDeNotaCreditoProveedor(idNotaCredito);
    }
    
    @GetMapping("/notas/renglones/debito/proveedores/{idNotaDebito}")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public List<RenglonNotaDebito> getRenglonesDeNotaDebitoProveedor(@PathVariable long idNotaDebito) {
        return notaService.getRenglonesDeNotaDebitoProveedor(idNotaDebito);
    }

    @GetMapping("/notas/credito/clientes/busqueda/criteria")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
    public Page<NotaCreditoCliente> getNotasCreditoClientes(@RequestParam Long idEmpresa,
                                                            @RequestParam(required = false) Long desde,
                                                            @RequestParam(required = false) Long hasta,
                                                            @RequestParam(required = false) Long idCliente,
                                                            @RequestParam(required = false) Integer nroSerie,
                                                            @RequestParam(required = false) Integer nroNota,
                                                            @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
                                                            @RequestParam(required = false) Long idUsuario,
                                                            @RequestParam(required = false) Integer pagina,
                                                            @RequestParam(required = false) Integer tamanio,
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
        Pageable pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.DESC, "fecha"));
        BusquedaNotaCriteria criteria = BusquedaNotaCriteria.builder()
                .idEmpresa(idEmpresa)
                .buscaPorFecha((desde != null) && (hasta != null))
                .fechaDesde(fechaDesde.getTime())
                .fechaHasta(fechaHasta.getTime())
                .buscaPorCliente(idCliente != null)
                .idCliente(idCliente)
                .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
                .numSerie((nroSerie != null) ? nroSerie : 0)
                .numNota((nroNota != null) ? nroNota : 0)
                .buscaPorTipoComprobante(tipoDeComprobante != null)
                .tipoComprobante(tipoDeComprobante)
                .buscaUsuario(idUsuario != null)
                .idUsuario(idUsuario)
                .pageable(pageable)
                .build();
        Claims claims =
                Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
        return notaService.buscarNotaCreditoCliente(criteria, (int) claims.get("idUsuario"));
    }

    @GetMapping("/notas/credito/proveedores/busqueda/criteria")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Page<NotaCreditoProveedor> getNotasCreditoProveedores(@RequestParam Long idEmpresa,
                                                            @RequestParam(required = false) Long desde,
                                                            @RequestParam(required = false) Long hasta,
                                                            @RequestParam(required = false) Long idProveedor,
                                                            @RequestParam(required = false) Integer nroSerie,
                                                            @RequestParam(required = false) Integer nroNota,
                                                            @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
                                                            @RequestParam(required = false) Long idUsuario,
                                                            @RequestParam(required = false) Integer pagina,
                                                            @RequestParam(required = false) Integer tamanio,
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
        Pageable pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.DESC, "fecha"));
        BusquedaNotaCriteria criteria = BusquedaNotaCriteria.builder()
                .idEmpresa(idEmpresa)
                .buscaPorFecha((desde != null) && (hasta != null))
                .fechaDesde(fechaDesde.getTime())
                .fechaHasta(fechaHasta.getTime())
                .buscaPorCliente(idProveedor != null)
                .idCliente(idProveedor)
                .buscaPorNumeroNota((nroSerie != null) && (nroNota != null))
                .numSerie((nroSerie != null) ? nroSerie : 0)
                .numNota((nroNota != null) ? nroNota : 0)
                .buscaPorTipoComprobante(tipoDeComprobante != null)
                .tipoComprobante(tipoDeComprobante)
                .buscaUsuario(idUsuario != null)
                .idUsuario(idUsuario)
                .pageable(pageable)
                .build();
        Claims claims =
                Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
        return notaService.buscarNotaCreditoProveedor(criteria, (int) claims.get("idUsuario"));
    }
    
    @PostMapping("/notas/credito/empresa/{idEmpresa}/cliente/{idCliente}/usuario/{idUsuario}/factura/{idFactura}")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public Nota guardarNotaCreditoCliente(@RequestBody NotaCredito nota,
                                   @PathVariable long idEmpresa,
                                   @PathVariable long idCliente,
                                   @PathVariable long idUsuario,
                                   @PathVariable long idFactura, 
                                   @RequestParam boolean modificarStock) {
        return notaService.guardarNotaCliente(nota, idEmpresa, idCliente, idUsuario, null, idFactura, modificarStock);
    }
    
    @PostMapping("/notas/debito/empresa/{idEmpresa}/cliente/{idCliente}/usuario/{idUsuario}/recibo/{idRecibo}")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public Nota guardarNotaDebitoCliente(@RequestBody NotaDebito nota,
                                  @PathVariable long idEmpresa,
                                  @PathVariable long idCliente,
                                  @PathVariable long idUsuario,
                                  @PathVariable long idRecibo) {
        return notaService.guardarNotaCliente(nota, idEmpresa, idCliente, idUsuario, idRecibo, null, false);
    }
    
    @PostMapping("/notas/credito/empresa/{idEmpresa}/proveedor/{idProveedor}/usuario/{idUsuario}/factura/{idFactura}")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Nota guardarNotaCreditoProveedor(@RequestBody NotaCredito nota,
                                   @PathVariable long idEmpresa,
                                   @PathVariable long idProveedor,
                                   @PathVariable long idUsuario,
                                   @PathVariable long idFactura, 
                                   @RequestParam boolean modificarStock) {
        return notaService.guardarNotaProveedor(nota, idEmpresa, idProveedor, idUsuario, null, idFactura, modificarStock);
    }
    
    @PostMapping("/notas/debito/empresa/{idEmpresa}/proveedor/{idProveedor}/usuario/{idUsuario}/recibo/{idRecibo}")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Nota guardarNotaDebitoProveedor(@RequestBody NotaDebito nota,
                                  @PathVariable long idEmpresa,
                                  @PathVariable long idProveedor,
                                  @PathVariable long idUsuario,
                                  @PathVariable long idRecibo) {
        return notaService.guardarNotaProveedor(nota, idEmpresa, idProveedor, idUsuario, idRecibo, null, false);
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
    
}
