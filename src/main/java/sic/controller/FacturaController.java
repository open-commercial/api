package sic.controller;

import java.math.BigDecimal;
import java.util.*;

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
import sic.service.IClienteService;
import sic.service.IEmpresaService;
import sic.service.IFacturaService;
import sic.service.IPedidoService;
import sic.service.IProveedorService;
import sic.service.IUsuarioService;
import sic.service.IReciboService;
import sic.service.ITransportistaService;

@RestController
@RequestMapping("/api/v1")
public class FacturaController {
    
    private final IFacturaService facturaService;
    private final IEmpresaService empresaService;
    private final IProveedorService proveedorService;
    private final IClienteService clienteService;
    private final IUsuarioService usuarioService;
    private final IPedidoService pedidoService;
    private final ITransportistaService transportistaService;
    private final IReciboService reciboService;
    private final int TAMANIO_PAGINA_DEFAULT = 50;

    @Value("${SIC_JWT_KEY}")
    private String secretkey;
    
    @Autowired
    public FacturaController(IFacturaService facturaService, IEmpresaService empresaService,
                             IProveedorService proveedorService, IClienteService clienteService,
                             IUsuarioService usuarioService, IPedidoService pedidoService,
                             ITransportistaService transportistaService, IReciboService reciboService) {
        this.facturaService = facturaService;
        this.empresaService = empresaService;
        this.proveedorService = proveedorService;
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
        this.pedidoService = pedidoService;    
        this.transportistaService = transportistaService;
        this.reciboService = reciboService;
    }
    
    @GetMapping("/facturas/{idFactura}")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public Factura getFacturaPorId(@PathVariable long idFactura) {
        return facturaService.getFacturaPorId(idFactura);
    }
    
    @PostMapping("/facturas/venta")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public List<Factura> guardarFacturaVenta(@RequestBody FacturaVenta fv,
                                             @RequestParam Long idEmpresa, 
                                             @RequestParam Long idCliente,
                                             @RequestParam Long idUsuario,
                                             @RequestParam Long idTransportista,
                                             @RequestParam(required = false) long[] idsFormaDePago,
                                             @RequestParam(required = false) BigDecimal[] montos,
                                             @RequestParam(required = false) int[] indices,
                                             @RequestParam(required = false) Long idPedido) {
        Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
        fv.setEmpresa(empresa);
        fv.setCliente(clienteService.getClientePorId(idCliente));
        fv.setUsuario(usuarioService.getUsuarioPorId(idUsuario));
        fv.setTransportista(transportistaService.getTransportistaPorId(idTransportista));
        if (indices != null) {
            return facturaService.guardar(facturaService.dividirFactura((FacturaVenta) fv, indices), idPedido, 
                    reciboService.construirRecibos(idsFormaDePago, empresa,
                            fv.getCliente(), fv.getUsuario(), montos, fv.getTotal(), fv.getFecha()));
        } else {
            List<Factura> facturas = new ArrayList<>();
            facturas.add(fv);
            return facturaService.guardar(facturas, idPedido, reciboService.construirRecibos(idsFormaDePago, empresa,
                            fv.getCliente(), fv.getUsuario(), montos, fv.getTotal(), fv.getFecha()));         
        }
    }   
    
    @PostMapping("/facturas/compra")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public List<Factura> guardarFacturaCompra(@RequestBody FacturaCompra fc,
                                              @RequestParam Long idEmpresa,
                                              @RequestParam Long idProveedor,
                                              @RequestParam Long idTransportista) {
            fc.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
            fc.setProveedor(proveedorService.getProveedorPorId(idProveedor));
            fc.setTransportista(transportistaService.getTransportistaPorId(idTransportista));
            List<Factura> facturas = new ArrayList<>();
            facturas.add(fc);
            return facturaService.guardar(facturas, null, null);         
    }   
    
    @PostMapping("/facturas/{idFactura}/autorizacion")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public FacturaVenta autorizarFactura(@PathVariable long idFactura) {
        return facturaService.autorizarFacturaVenta((FacturaVenta) facturaService.getFacturaPorId(idFactura));
    }
    
    @DeleteMapping("/facturas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
    public void eliminar(@RequestParam long[] idFactura) {
        facturaService.eliminar(idFactura);
    }
        
    @GetMapping("/facturas/{idFactura}/renglones")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public List<RenglonFactura> getRenglonesDeLaFactura(@PathVariable long idFactura) {
            return facturaService.getRenglonesDeLaFactura(idFactura);
    }
    
    @GetMapping("/facturas/{idFactura}/renglones/notas/credito") 
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public List<RenglonFactura> getRenglonesDeLaFacturaModificadosParaCredito(@PathVariable long idFactura) {
            return facturaService.getRenglonesDeLaFacturaModificadosParaCredito(idFactura);
    }
    
    @GetMapping("/facturas/compra/busqueda/criteria")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Page<FacturaCompra> buscarFacturaCompra(@RequestParam Long idEmpresa,
                                                   @RequestParam(required = false) Long desde,
                                                   @RequestParam(required = false) Long hasta,
                                                   @RequestParam(required = false) Long idProveedor,
                                                   @RequestParam(required = false) Integer nroSerie,
                                                   @RequestParam(required = false) Integer nroFactura,
                                                   @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
                                                   @RequestParam(required = false) Integer pagina,
                                                   @RequestParam(required = false) Integer tamanio) {
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
        BusquedaFacturaCompraCriteria criteria = BusquedaFacturaCompraCriteria.builder()
                                                 .idEmpresa(idEmpresa)
                                                 .buscaPorFecha((desde != null) && (hasta != null))
                                                 .fechaDesde(fechaDesde.getTime())
                                                 .fechaHasta(fechaHasta.getTime())
                                                 .buscaPorProveedor(idProveedor != null)
                                                 .idProveedor(idProveedor)
                                                 .buscaPorNumeroFactura((nroSerie != null) && (nroFactura != null))
                                                 .numSerie((nroSerie != null) ? nroSerie : 0)
                                                 .numFactura((nroFactura != null) ? nroFactura : 0)
                                                 .buscaPorTipoComprobante(tipoDeComprobante != null)
                                                 .tipoComprobante(tipoDeComprobante)
                                                 .cantRegistros(0)
                                                 .pageable(pageable)
                                                 .build();
        return facturaService.buscarFacturaCompra(criteria);
    }

    @GetMapping("/facturas/venta/busqueda/criteria")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public Page<FacturaVenta> buscarFacturaVenta(@RequestParam Long idEmpresa,
                                                 @RequestParam(required = false) Long desde,
                                                 @RequestParam(required = false) Long hasta,
                                                 @RequestParam(required = false) Long idCliente,
                                                 @RequestParam(required = false) Integer nroSerie,
                                                 @RequestParam(required = false) Integer nroFactura,                                                 
                                                 @RequestParam(required = false) Long idViajante,
                                                 @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
                                                 @RequestParam(required = false) Long idUsuario,
                                                 @RequestParam(required = false) Long nroPedido,
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
        BusquedaFacturaVentaCriteria criteria = BusquedaFacturaVentaCriteria.builder()
                .idEmpresa(idEmpresa)
                .buscaPorFecha((desde != null) && (hasta != null))
                .fechaDesde(fechaDesde.getTime())
                .fechaHasta(fechaHasta.getTime())
                .buscaCliente(idCliente != null)
                .idCliente(idCliente)
                .buscaUsuario(idUsuario != null)
                .idUsuario(idUsuario)
                .buscaViajante(idViajante != null)
                .idViajante(idViajante)
                .buscaPorNumeroFactura((nroSerie != null) && (nroFactura != null))
                .numSerie((nroSerie != null) ? nroSerie : 0)
                .numFactura((nroFactura != null) ? nroFactura : 0)
                .buscarPorPedido(nroPedido != null)
                .nroPedido((nroPedido != null) ? nroPedido : 0)
                .buscaPorTipoComprobante(tipoDeComprobante != null)
                .tipoComprobante(tipoDeComprobante)
                .cantRegistros(0)
                .pageable(pageable)
                .build();
        Claims claims =
                Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
        return facturaService.buscarFacturaVenta(criteria, (int) claims.get("idUsuario"));
    }
    
    @GetMapping("/facturas/compra/tipos/empresas/{idEmpresa}/proveedores/{idProveedor}")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public TipoDeComprobante[] getTipoFacturaCompra(@PathVariable long idEmpresa, @PathVariable long idProveedor) {
        return facturaService.getTipoFacturaCompra(empresaService.getEmpresaPorId(idEmpresa), proveedorService.getProveedorPorId(idProveedor));
    }

  @GetMapping("/facturas/venta/tipos/empresas/{idEmpresa}/clientes/{idCliente}")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public TipoDeComprobante[] getTipoFacturaVenta(
      @PathVariable long idEmpresa,
      @PathVariable long idCliente,
      @RequestHeader("Authorization") String token) {
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
    long idUsuario = (int) claims.get("idUsuario");
    List<Rol> rolesDeUsuario =
        usuarioService.getUsuarioPorId(idUsuario).getRoles();
    if (rolesDeUsuario.contains(Rol.ADMINISTRADOR)
        || rolesDeUsuario.contains(Rol.ENCARGADO)
        || rolesDeUsuario.contains(Rol.VENDEDOR)) {
      return facturaService.getTipoFacturaVenta(
          empresaService.getEmpresaPorId(idEmpresa), clienteService.getClientePorId(idCliente));
    } else if (rolesDeUsuario.contains(Rol.VIAJANTE)
            || rolesDeUsuario.contains(Rol.COMPRADOR)) {
      return new TipoDeComprobante[] {TipoDeComprobante.PEDIDO};
    }
    return null;
  }

    @GetMapping("/facturas/tipos/empresas/{idEmpresa}")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public TipoDeComprobante[] getTiposFacturaSegunEmpresa(@PathVariable long idEmpresa) {
        return facturaService.getTiposFacturaSegunEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    }    
    
    @GetMapping("/facturas/{idFactura}/reporte")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public ResponseEntity<byte[]> getReporteFacturaVenta(@PathVariable long idFactura) {        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);        
        headers.add("content-disposition", "inline; filename=Factura.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        byte[] reportePDF = facturaService.getReporteFacturaVenta(facturaService.getFacturaPorId(idFactura));
        return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
    }
    
    @GetMapping("/facturas/renglones/pedidos/{idPedido}") 
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
    public List<RenglonFactura> getRenglonesPedidoParaFacturar(@PathVariable long idPedido,
                                                               @RequestParam TipoDeComprobante tipoDeComprobante) {
        return facturaService.convertirRenglonesPedidoEnRenglonesFactura(pedidoService.getPedidoPorId(idPedido), tipoDeComprobante);
    } 
    
    @GetMapping("/facturas/renglon")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
    public RenglonFactura calcularRenglon(@RequestParam long idProducto,
                                          @RequestParam TipoDeComprobante tipoDeComprobante,
                                          @RequestParam Movimiento movimiento,
                                          @RequestParam BigDecimal cantidad, 
                                          @RequestParam BigDecimal descuentoPorcentaje) {
        return facturaService.calcularRenglon(tipoDeComprobante, movimiento, cantidad, idProducto, descuentoPorcentaje, false);
    }
        
    @GetMapping("/facturas/total-facturado-venta/criteria")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public BigDecimal calcularTotalFacturadoVenta(@RequestParam Long idEmpresa,
                                                  @RequestParam(required = false) Long desde,
                                                  @RequestParam(required = false) Long hasta,
                                                  @RequestParam(required = false) Long idCliente,
                                                  @RequestParam(required = false) Integer nroSerie,
                                                  @RequestParam(required = false) Integer nroFactura,
                                                  @RequestParam(required = false) Long idViajante,
                                                  @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
                                                  @RequestParam(required = false) Long idUsuario,
                                                  @RequestParam(required = false) Long nroPedido,
                                                  @RequestHeader("Authorization") String token) {
        Calendar fechaDesde = Calendar.getInstance();
        Calendar fechaHasta = Calendar.getInstance();
        if ((desde != null) && (hasta != null)) {
            fechaDesde.setTimeInMillis(desde);
            fechaHasta.setTimeInMillis(hasta);
        }
        BusquedaFacturaVentaCriteria criteria = BusquedaFacturaVentaCriteria.builder()
                                                 .idEmpresa(idEmpresa)
                                                 .buscaPorFecha((desde != null) && (hasta != null))
                                                 .fechaDesde(fechaDesde.getTime())
                                                 .fechaHasta(fechaHasta.getTime())
                                                 .buscaCliente(idCliente != null)
                                                 .idCliente(idCliente)
                                                 .buscaUsuario(idUsuario != null)
                                                 .idUsuario(idUsuario)
                                                 .buscaViajante(idViajante != null)
                                                 .idViajante(idViajante)
                                                 .buscaPorNumeroFactura((nroSerie != null) && (nroFactura != null))
                                                 .numSerie((nroSerie != null)? nroSerie : 0)
                                                 .numFactura((nroFactura != null) ? nroFactura : 0)
                                                 .buscarPorPedido(nroPedido != null)
                                                 .nroPedido((nroPedido != null) ? nroPedido : 0)
                                                 .buscaPorTipoComprobante(tipoDeComprobante != null)
                                                 .tipoComprobante(tipoDeComprobante)
                                                 .cantRegistros(0)
                                                 .build();
        Claims claims =
                Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
        return facturaService.calcularTotalFacturadoVenta(criteria, (int) claims.get("idUsuario"));
    }
    
    @GetMapping("/facturas/total-facturado-compra/criteria")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public BigDecimal calcularTotalFacturadoCompra(@RequestParam Long idEmpresa,
                                                   @RequestParam(required = false) Long desde,
                                                   @RequestParam(required = false) Long hasta,
                                                   @RequestParam(required = false) Long idProveedor,
                                                   @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
                                                   @RequestParam(required = false) Integer nroSerie,
                                                   @RequestParam(required = false) Integer nroFactura) {
        Calendar fechaDesde = Calendar.getInstance();
        Calendar fechaHasta = Calendar.getInstance();
        if ((desde != null) && (hasta != null)) {
            fechaDesde.setTimeInMillis(desde);            
            fechaHasta.setTimeInMillis(hasta);
        }
        BusquedaFacturaCompraCriteria criteria = BusquedaFacturaCompraCriteria.builder()
                                                 .idEmpresa(idEmpresa)
                                                 .buscaPorFecha((desde != null) && (hasta != null))
                                                 .fechaDesde(fechaDesde.getTime())
                                                 .fechaHasta(fechaHasta.getTime())
                                                 .buscaPorProveedor(idProveedor != null)
                                                 .idProveedor(idProveedor)
                                                 .buscaPorTipoComprobante(tipoDeComprobante != null)
                                                 .tipoComprobante(tipoDeComprobante)
                                                 .buscaPorNumeroFactura((nroSerie != null) && (nroFactura != null))
                                                 .numSerie((nroSerie != null) ? nroSerie : 0)
                                                 .numFactura((nroFactura != null) ? nroFactura : 0)
                                                 .cantRegistros(0)
                                                 .build();
        return facturaService.calcularTotalFacturadoCompra(criteria);
    }
    
    @GetMapping("/facturas/total-iva-venta/criteria")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public BigDecimal calcularIvaVenta(@RequestParam Long idEmpresa,
                                       @RequestParam(required = false) Long desde,
                                       @RequestParam(required = false) Long hasta,
                                       @RequestParam(required = false) Long idCliente,
                                       @RequestParam(required = false) Integer nroSerie,
                                       @RequestParam(required = false) Integer nroFactura,                                   
                                       @RequestParam(required = false) Long idViajante,
                                       @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
                                       @RequestParam(required = false) Long idUsuario,
                                       @RequestParam(required = false) Long nroPedido,
                                       @RequestHeader("Authorization") String token) {
        Calendar fechaDesde = Calendar.getInstance();
        Calendar fechaHasta = Calendar.getInstance();
        if ((desde != null) && (hasta != null)) {
            fechaDesde.setTimeInMillis(desde);
            fechaHasta.setTimeInMillis(hasta);
        }
        BusquedaFacturaVentaCriteria criteria = BusquedaFacturaVentaCriteria.builder()
                                                .idEmpresa(idEmpresa)
                                                .buscaPorFecha((desde != null) && (hasta != null))
                                                .fechaDesde(fechaDesde.getTime())
                                                .fechaHasta(fechaHasta.getTime())
                                                .buscaCliente(idCliente != null)
                                                .idCliente(idCliente)
                                                .buscaUsuario(idUsuario != null)
                                                .idUsuario(idUsuario)
                                                .buscaViajante(idViajante != null)
                                                .idViajante(idViajante)
                                                .buscaPorNumeroFactura((nroSerie != null) && (nroFactura != null))
                                                .numSerie((nroSerie != null) ? nroSerie : 0)
                                                .numFactura((nroFactura != null) ? nroFactura : 0)
                                                .buscarPorPedido(nroPedido != null)
                                                .nroPedido((nroPedido != null) ? nroPedido : 0)
                                                .buscaPorTipoComprobante(tipoDeComprobante != null)
                                                .tipoComprobante(tipoDeComprobante)
                                                .cantRegistros(0)
                                                .build();
        Claims claims =
                Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
        return facturaService.calcularIvaVenta(criteria, (int) claims.get("idUsuario"));
    }
    
    @GetMapping("/facturas/total-iva-compra/criteria")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public BigDecimal calcularTotalIvaCompra(@RequestParam Long idEmpresa,
                                             @RequestParam(required = false) Long desde,
                                             @RequestParam(required = false) Long hasta,
                                             @RequestParam(required = false) Long idProveedor,
                                             @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
                                             @RequestParam(required = false) Integer nroSerie,
                                             @RequestParam(required = false) Integer nroFactura) {
        Calendar fechaDesde = Calendar.getInstance();
        Calendar fechaHasta = Calendar.getInstance();
        if ((desde != null) && (hasta != null)) {
            fechaDesde.setTimeInMillis(desde);            
            fechaHasta.setTimeInMillis(hasta);
        }
        BusquedaFacturaCompraCriteria criteria = BusquedaFacturaCompraCriteria.builder()
                                                 .idEmpresa(idEmpresa)
                                                 .buscaPorFecha((desde != null) && (hasta != null))
                                                 .fechaDesde(fechaDesde.getTime())
                                                 .fechaHasta(fechaHasta.getTime())
                                                 .buscaPorProveedor(idProveedor != null)
                                                 .idProveedor(idProveedor)
                                                 .buscaPorTipoComprobante(tipoDeComprobante != null)
                                                 .tipoComprobante(tipoDeComprobante)
                                                 .buscaPorNumeroFactura((nroSerie != null) && (nroFactura != null))
                                                 .numSerie((nroSerie != null) ? nroSerie : 0)
                                                 .numFactura((nroFactura != null) ? nroFactura : 0)
                                                 .cantRegistros(0)
                                                 .build();
        return facturaService.calcularIvaCompra(criteria);
    }
    
    @GetMapping("/facturas/ganancia-total/criteria")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public BigDecimal calcularGananciaTotal(@RequestParam Long idEmpresa,
                                            @RequestParam(required = false) Long desde,
                                            @RequestParam(required = false) Long hasta,
                                            @RequestParam(required = false) Long idCliente,
                                            @RequestParam(required = false) Integer nroSerie,
                                            @RequestParam(required = false) Integer nroFactura,                                        
                                            @RequestParam(required = false) Long idViajante,
                                            @RequestParam(required = false) TipoDeComprobante tipoDeComprobante,
                                            @RequestParam(required = false) Long idUsuario,
                                            @RequestParam(required = false) Long nroPedido,
                                            @RequestHeader("Authorization") String token) {
        Calendar fechaDesde = Calendar.getInstance();
        Calendar fechaHasta = Calendar.getInstance();
        if ((desde != null) && (hasta != null)) {
            fechaDesde.setTimeInMillis(desde);
            fechaHasta.setTimeInMillis(hasta);
        }
        BusquedaFacturaVentaCriteria criteria = BusquedaFacturaVentaCriteria.builder()
                                                 .idEmpresa(idEmpresa)
                                                 .buscaPorFecha((desde != null) && (hasta != null))
                                                 .fechaDesde(fechaDesde.getTime())
                                                 .fechaHasta(fechaHasta.getTime())
                                                 .buscaCliente(idCliente != null)
                                                 .idCliente(idCliente)
                                                 .buscaUsuario(idUsuario != null)
                                                 .idUsuario(idUsuario)
                                                 .buscaViajante(idViajante != null)
                                                 .idViajante(idViajante)
                                                 .buscaPorNumeroFactura((nroSerie != null) && (nroFactura != null))
                                                 .numSerie((nroSerie != null)? nroSerie : 0)
                                                 .numFactura((nroFactura != null) ? nroFactura : 0)
                                                 .buscarPorPedido(nroPedido != null)
                                                 .nroPedido((nroPedido != null) ? nroPedido : 0)
                                                 .buscaPorTipoComprobante(tipoDeComprobante != null)
                                                 .tipoComprobante(tipoDeComprobante)
                                                 .cantRegistros(0)
                                                 .build();
        Claims claims =
                Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
        return facturaService.calcularGananciaTotal(criteria, (int) claims.get("idUsuario"));
    }
}
