package sic.controller;

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
import sic.modelo.BusquedaReciboCriteria;
import sic.modelo.Movimiento;
import sic.modelo.Recibo;
import sic.modelo.Rol;
import sic.modelo.dto.ReciboDTO;
import sic.service.*;

import java.math.BigDecimal;
import java.util.Calendar;

@RestController
@RequestMapping("/api/v1")
public class ReciboController {
    
    private final IReciboService reciboService;
    private final IEmpresaService empresaService;
    private final IUsuarioService usuarioService;
    private final IClienteService clienteService;
    private final IProveedorService proveedorService;
    private final IFormaDePagoService formaDePagoService;
    private final IAuthService authService;
    private final ModelMapper modelMapper;
    private static final int TAMANIO_PAGINA_DEFAULT = 25;
    
    @Autowired
    public ReciboController(IReciboService reciboService, IEmpresaService empresaService,
                            IUsuarioService usuarioService, IClienteService clienteService,
                            IProveedorService proveedorService, IFormaDePagoService formaDePagoService,
                            IAuthService authService, ModelMapper modelMapper) {
        this.reciboService = reciboService;
        this.empresaService = empresaService;
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
        this.formaDePagoService = formaDePagoService;
        this.proveedorService = proveedorService;
        this.authService = authService;
        this.modelMapper = modelMapper;
    }
    
    @GetMapping("/recibos/{idRecibo}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public Recibo getReciboPorId(@PathVariable long idRecibo) {
        return reciboService.getById(idRecibo);
    }

  @GetMapping("/recibos/venta/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<Recibo> buscarConCriteriaVenta(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) String concepto,
      @RequestParam(required = false) Integer nroSerie,
      @RequestParam(required = false) Integer nroRecibo,
      @RequestParam(required = false) Long idCliente,
      @RequestParam(required = false) Long idUsuario,
      @RequestParam(required = false) Long idViajante,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable;
    if (ordenarPor == null || sentido == null) {
      pageable =
          new PageRequest(pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, "fecha"));
    } else {
      switch (sentido) {
        case "ASC":
          pageable =
              new PageRequest(
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
          break;
        case "DESC":
          pageable =
              new PageRequest(
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
          break;
        default:
          pageable =
              new PageRequest(
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, "fecha"));
          break;
      }
    }
    BusquedaReciboCriteria criteria =
        BusquedaReciboCriteria.builder()
            .buscaPorFecha((desde != null) && (hasta != null))
            .fechaDesde(fechaDesde.getTime())
            .fechaHasta(fechaHasta.getTime())
            .buscaPorConcepto(concepto != null)
            .concepto(concepto)
            .buscaPorNumeroRecibo((nroSerie != null) && (nroRecibo != null))
            .numSerie((nroSerie != null) ? nroSerie : 0)
            .numRecibo((nroRecibo != null) ? nroRecibo : 0)
            .buscaPorCliente(idCliente != null)
            .idCliente(idCliente)
            .buscaPorUsuario(idUsuario != null)
            .idUsuario(idUsuario)
            .buscaPorViajante(idViajante != null)
            .idViajante(idViajante)
            .idEmpresa(idEmpresa)
            .pageable(pageable)
            .movimiento(Movimiento.VENTA)
            .build();
    return reciboService.buscarRecibos(criteria);
  }

  @GetMapping("/recibos/compra/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<Recibo> buscarConCriteriaCompra(
      @RequestParam Long idEmpresa,
      @RequestParam(required = false) Long desde,
      @RequestParam(required = false) Long hasta,
      @RequestParam(required = false) String concepto,
      @RequestParam(required = false) Integer nroSerie,
      @RequestParam(required = false) Integer nroRecibo,
      @RequestParam(required = false) Long idProveedor,
      @RequestParam(required = false) Long idUsuario,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable;
    if (ordenarPor == null || sentido == null) {
      pageable =
          new PageRequest(pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, "fecha"));
    } else {
      switch (sentido) {
        case "ASC":
          pageable =
              new PageRequest(
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
          break;
        case "DESC":
          pageable =
              new PageRequest(
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
          break;
        default:
          pageable =
              new PageRequest(
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, "fecha"));
          break;
      }
    }
    BusquedaReciboCriteria criteria =
        BusquedaReciboCriteria.builder()
            .buscaPorFecha((desde != null) && (hasta != null))
            .fechaDesde(fechaDesde.getTime())
            .fechaHasta(fechaHasta.getTime())
            .buscaPorConcepto(concepto != null)
            .buscaPorNumeroRecibo((nroSerie != null) && (nroRecibo != null))
            .numSerie((nroSerie != null) ? nroSerie : 0)
            .numRecibo((nroRecibo != null) ? nroRecibo : 0)
            .buscaPorConcepto(concepto != null)
            .concepto(concepto)
            .buscaPorProveedor(idProveedor != null)
            .idProveedor(idProveedor)
            .buscaPorUsuario(idUsuario != null)
            .idUsuario(idUsuario)
            .idEmpresa(idEmpresa)
            .pageable(pageable)
            .movimiento(Movimiento.COMPRA)
            .build();
    return reciboService.buscarRecibos(criteria);
  }

  @GetMapping("/recibos/compra/total/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalRecibosCompra(@RequestParam Long idEmpresa,
                                          @RequestParam(required = false) Long desde,
                                          @RequestParam(required = false) Long hasta,
                                          @RequestParam(required = false) String concepto,
                                          @RequestParam(required = false) Integer nroSerie,
                                          @RequestParam(required = false) Integer nroRecibo,
                                          @RequestParam(required = false) Long idProveedor,
                                          @RequestParam(required = false) Long idUsuario) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    BusquedaReciboCriteria criteria =
      BusquedaReciboCriteria.builder()
        .buscaPorFecha((desde != null) && (hasta != null))
        .fechaDesde(fechaDesde.getTime())
        .fechaHasta(fechaHasta.getTime())
        .buscaPorConcepto(concepto != null)
        .buscaPorNumeroRecibo((nroSerie != null) && (nroRecibo != null))
        .numSerie((nroSerie != null) ? nroSerie : 0)
        .numRecibo((nroRecibo != null) ? nroRecibo : 0)
        .buscaPorConcepto(concepto != null)
        .concepto(concepto)
        .buscaPorProveedor(idProveedor != null)
        .idProveedor(idProveedor)
        .buscaPorUsuario(idUsuario != null)
        .idUsuario(idUsuario)
        .idEmpresa(idEmpresa)
        .movimiento(Movimiento.COMPRA)
        .build();
    return reciboService.getTotalRecibos(criteria);
  }

  @GetMapping("/recibos/venta/total/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalRecibosVenta(@RequestParam Long idEmpresa,
                                         @RequestParam(required = false) Long desde,
                                         @RequestParam(required = false) Long hasta,
                                         @RequestParam(required = false) String concepto,
                                         @RequestParam(required = false) Integer nroSerie,
                                         @RequestParam(required = false) Integer nroRecibo,
                                         @RequestParam(required = false) Long idCliente,
                                         @RequestParam(required = false) Long idUsuario) {
    Calendar fechaDesde = Calendar.getInstance();
    Calendar fechaHasta = Calendar.getInstance();
    if ((desde != null) && (hasta != null)) {
      fechaDesde.setTimeInMillis(desde);
      fechaHasta.setTimeInMillis(hasta);
    }
    BusquedaReciboCriteria criteria =
      BusquedaReciboCriteria.builder()
        .buscaPorFecha((desde != null) && (hasta != null))
        .fechaDesde(fechaDesde.getTime())
        .fechaHasta(fechaHasta.getTime())
        .buscaPorConcepto(concepto != null)
        .buscaPorNumeroRecibo((nroSerie != null) && (nroRecibo != null))
        .numSerie((nroSerie != null) ? nroSerie : 0)
        .numRecibo((nroRecibo != null) ? nroRecibo : 0)
        .buscaPorConcepto(concepto != null)
        .concepto(concepto)
        .buscaPorCliente(idCliente != null)
        .idCliente(idCliente)
        .buscaPorUsuario(idUsuario != null)
        .idUsuario(idUsuario)
        .idEmpresa(idEmpresa)
        .movimiento(Movimiento.VENTA)
        .build();
    return reciboService.getTotalRecibos(criteria);
  }

  @PostMapping("/recibos/clientes")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Recibo guardarReciboCliente(
      @RequestBody ReciboDTO reciboDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    Recibo recibo = modelMapper.map(reciboDTO, Recibo.class);
    recibo.setEmpresa(empresaService.getEmpresaPorId(reciboDTO.getIdEmpresa()));
    recibo.setCliente(clienteService.getClientePorId(reciboDTO.getIdCliente()));
    recibo.setFormaDePago(formaDePagoService.getFormasDePagoPorId(reciboDTO.getIdFormaDePago()));
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    recibo.setUsuario(
        usuarioService.getUsuarioPorId(((Integer) claims.get("idUsuario")).longValue()));
    return reciboService.guardar(recibo);
  }

  @PostMapping("/recibos/proveedores")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Recibo guardarReciboProveedor(
      @RequestBody ReciboDTO reciboDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    Recibo recibo = modelMapper.map(reciboDTO, Recibo.class);
    recibo.setEmpresa(empresaService.getEmpresaPorId(reciboDTO.getIdEmpresa()));
    recibo.setProveedor(proveedorService.getProveedorPorId(reciboDTO.getIdProveedor()));
    recibo.setFormaDePago(formaDePagoService.getFormasDePagoPorId(reciboDTO.getIdFormaDePago()));
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    recibo.setUsuario(
        usuarioService.getUsuarioPorId(((Integer) claims.get("idUsuario")).longValue()));
    return reciboService.guardar(recibo);
  }

    @DeleteMapping("/recibos/{idRecibo}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR})
    public void eliminar(@PathVariable long idRecibo) {
        reciboService.eliminar(idRecibo);
    }
    
    @GetMapping("/recibos/{idRecibo}/reporte")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public ResponseEntity<byte[]> getReporteRecibo(@PathVariable long idRecibo) {        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);        
        headers.add("content-disposition", "inline; filename=Recibo.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        byte[] reportePDF = reciboService.getReporteRecibo(reciboService.getById(idRecibo));
        return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
    }
    
}
