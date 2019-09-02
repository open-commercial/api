package sic.controller;

import java.math.BigDecimal;
import java.util.Locale;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
import sic.modelo.criteria.BusquedaCuentaCorrienteClienteCriteria;
import sic.modelo.criteria.BusquedaCuentaCorrienteProveedorCriteria;
import sic.service.*;
import sic.exception.BusinessServiceException;

@RestController
@RequestMapping("/api/v1")
public class CuentaCorrienteController {

  private final ICuentaCorrienteService cuentaCorrienteService;
  private final IProveedorService proveedorService;
  private final IClienteService clienteService;
  private final IAuthService authService;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final MessageSource messageSource;

  @Autowired
  public CuentaCorrienteController(
      ICuentaCorrienteService cuentaCorrienteService,
      IProveedorService proveedorService,
      IClienteService clienteService,
      IAuthService authService,
      MessageSource messageSource) {
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.clienteService = clienteService;
    this.proveedorService = proveedorService;
    this.authService = authService;
    this.messageSource = messageSource;
  }

  @GetMapping("/cuentas-corriente/clientes/busqueda/criteria")
  public Page<CuentaCorrienteCliente> buscarConCriteria(
      @RequestParam(required = false) String nroCliente,
      @RequestParam(required = false) String nombreFiscal,
      @RequestParam(required = false) String nombreFantasia,
      @RequestParam(required = false) Long idFiscal,
      @RequestParam(required = false) Long idViajante,
      @RequestParam(required = false) Long idProvincia,
      @RequestParam(required = false) Long idLocalidad,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestHeader("Authorization") String authorizationHeader) {
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable;
    if (ordenarPor == null || sentido == null) {
      pageable =
          PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, "cliente.nombreFiscal"));
    } else {
      switch (sentido) {
        case "ASC":
          pageable =
              PageRequest.of(
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
          break;
        case "DESC":
          pageable =
              PageRequest.of(
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
          break;
        default:
          pageable =
              PageRequest.of(
                  pagina,
                  TAMANIO_PAGINA_DEFAULT,
                  new Sort(Sort.Direction.ASC, "cliente.nombreFiscal"));
          break;
      }
    }
    BusquedaCuentaCorrienteClienteCriteria criteria =
        BusquedaCuentaCorrienteClienteCriteria.builder()
            .buscaPorNombreFiscal(nombreFiscal != null)
            .nombreFiscal(nombreFiscal)
            .buscaPorNombreFantasia(nombreFantasia != null)
            .nombreFantasia(nombreFantasia)
            .buscaPorIdFiscal(idFiscal != null)
            .idFiscal(idFiscal)
            .buscaPorViajante(idViajante != null)
            .idViajante(idViajante)
            .buscaPorProvincia(idProvincia != null)
            .idProvincia(idProvincia)
            .buscaPorLocalidad(idLocalidad != null)
            .idLocalidad(idLocalidad)
            .buscarPorNroDeCliente(nroCliente != null)
            .nroDeCliente(nroCliente)
            .pageable(pageable)
            .build();
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return cuentaCorrienteService.buscarCuentaCorrienteCliente(
        criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/cuentas-corriente/proveedores/busqueda/criteria")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
  })
  public Page<CuentaCorrienteProveedor> buscarConCriteria(
      @RequestParam(required = false) String nroProveedor,
      @RequestParam(required = false) String razonSocial,
      @RequestParam(required = false) Long idFiscal,
      @RequestParam(required = false) Long idProvincia,
      @RequestParam(required = false) Long idLocalidad,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido) {
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable;
    if (ordenarPor == null || sentido == null) {
      pageable =
          PageRequest.of(
              pagina,
              TAMANIO_PAGINA_DEFAULT,
              new Sort(Sort.Direction.ASC, "proveedor.razonSocial"));
    } else {
      switch (sentido) {
        case "ASC":
          pageable =
              PageRequest.of(
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
          break;
        case "DESC":
          pageable =
              PageRequest.of(
                  pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
          break;
        default:
          pageable =
              PageRequest.of(
                  pagina,
                  TAMANIO_PAGINA_DEFAULT,
                  new Sort(Sort.Direction.ASC, "proveedor.razonSocial"));
          break;
      }
    }
    BusquedaCuentaCorrienteProveedorCriteria criteria =
        BusquedaCuentaCorrienteProveedorCriteria.builder()
            .buscaPorNroProveedor(nroProveedor != null)
            .nroProveedor(nroProveedor)
            .buscaPorRazonSocial(razonSocial != null)
            .razonSocial(razonSocial)
            .buscaPorIdFiscal(idFiscal != null)
            .idFiscal(idFiscal)
            .buscaPorIdFiscal(idFiscal != null)
            .idFiscal(idFiscal)
            .buscaPorProvincia(idProvincia != null)
            .idProvincia(idProvincia)
            .buscaPorLocalidad(idLocalidad != null)
            .idLocalidad(idLocalidad)
            .pageable(pageable)
            .build();
    return cuentaCorrienteService.buscarCuentaCorrienteProveedor(criteria);
  }

  @GetMapping("/cuentas-corriente/clientes/{idCliente}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public CuentaCorrienteCliente getCuentaCorrientePorCliente(@PathVariable Long idCliente) {
    return cuentaCorrienteService.getCuentaCorrientePorCliente(
        clienteService.getClienteNoEliminadoPorId(idCliente));
  }

  @GetMapping("/cuentas-corriente/proveedores/{idProveedor}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public CuentaCorrienteProveedor getCuentaCorrientePorProveedor(@PathVariable Long idProveedor) {
    return cuentaCorrienteService.getCuentaCorrientePorProveedor(
        proveedorService.getProveedorNoEliminadoPorId(idProveedor));
  }

  @GetMapping("/cuentas-corriente/clientes/{idCliente}/saldo")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public BigDecimal getSaldoCuentaCorrienteCliente(@PathVariable long idCliente) {
    return cuentaCorrienteService
        .getCuentaCorrientePorCliente(clienteService.getClienteNoEliminadoPorId(idCliente))
        .getSaldo();
  }

  @GetMapping("/cuentas-corriente/proveedores/{idProveedor}/saldo")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public BigDecimal getSaldoCuentaCorrienteProveedor(@PathVariable long idProveedor) {
    return cuentaCorrienteService
        .getCuentaCorrientePorProveedor(proveedorService.getProveedorNoEliminadoPorId(idProveedor))
        .getSaldo();
  }

  @GetMapping("/cuentas-corriente/{idCuentaCorriente}/renglones")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(
      @PathVariable long idCuentaCorriente,
      @RequestParam(required = false) Integer pagina) {
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable = PageRequest.of(pagina, TAMANIO_PAGINA_DEFAULT);
    return cuentaCorrienteService.getRenglonesCuentaCorriente(idCuentaCorriente, pageable);
  }

  @GetMapping("/cuentas-corriente/clientes/{idCliente}/reporte")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public ResponseEntity<byte[]> getReporteCuentaCorrienteXls(
      @PathVariable long idCliente,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) String formato) {
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable = PageRequest.of(pagina, TAMANIO_PAGINA_DEFAULT);
    HttpHeaders headers = new HttpHeaders();
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    switch (formato) {
      case "xlsx":
        headers.setContentType(new MediaType("application", "vnd.ms-excel"));
        headers.set("Content-Disposition", "attachment; filename=EstadoCuentaCorriente.xlsx");
        byte[] reporteXls =
            cuentaCorrienteService.getReporteCuentaCorrienteCliente(
                cuentaCorrienteService.getCuentaCorrientePorCliente(
                    clienteService.getClienteNoEliminadoPorId(idCliente)),
                pageable,
                formato);
        headers.setContentLength(reporteXls.length);
        return new ResponseEntity<>(reporteXls, headers, HttpStatus.OK);
      case "pdf":
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add("Content-Disposition", "attachment; filename=EstadoCuentaCorriente.pdf");
        byte[] reportePDF =
            cuentaCorrienteService.getReporteCuentaCorrienteCliente(
                cuentaCorrienteService.getCuentaCorrientePorCliente(
                    clienteService.getClienteNoEliminadoPorId(idCliente)),
                pageable,
                formato);
        return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
      default:
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_formato_no_valido", null, Locale.getDefault()));
    }
  }
}
