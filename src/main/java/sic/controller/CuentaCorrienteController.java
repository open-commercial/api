package sic.controller;

import java.math.BigDecimal;
import java.util.Locale;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
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

  @PostMapping("/cuentas-corriente/clientes/busqueda/criteria")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE
  })
  public Page<CuentaCorrienteCliente> buscarCuentasCorrienteCliente(
      @RequestBody BusquedaCuentaCorrienteClienteCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelJWT(authorizationHeader);
    return cuentaCorrienteService.buscarCuentaCorrienteCliente(
        criteria, (int) claims.get("idUsuario"));
  }

  @PostMapping("/cuentas-corriente/proveedores/busqueda/criteria")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
  })
  public Page<CuentaCorrienteProveedor> buscarCuentasCorrienteProveedor(
      @RequestBody BusquedaCuentaCorrienteProveedorCriteria criteria) {
    return cuentaCorrienteService.buscarCuentaCorrienteProveedor(criteria);
  }

  @GetMapping("/cuentas-corriente/clientes/{idCliente}")
  public CuentaCorrienteCliente getCuentaCorrientePorCliente(@PathVariable Long idCliente) {
    return cuentaCorrienteService.getCuentaCorrientePorCliente(
        clienteService.getClienteNoEliminadoPorId(idCliente));
  }

  @GetMapping("/cuentas-corriente/clientes/predeterminado")
  public CuentaCorrienteCliente getCuentaCorrienteClientePredeterminado() {
    return cuentaCorrienteService.getCuentaCorrientePorCliente(
        clienteService.getClientePredeterminado());
  }

  @GetMapping("/cuentas-corriente/proveedores/{idProveedor}")
  public CuentaCorrienteProveedor getCuentaCorrientePorProveedor(@PathVariable Long idProveedor) {
    return cuentaCorrienteService.getCuentaCorrientePorProveedor(
        proveedorService.getProveedorNoEliminadoPorId(idProveedor));
  }

  @GetMapping("/cuentas-corriente/clientes/{idCliente}/saldo")
  public BigDecimal getSaldoCuentaCorrienteCliente(@PathVariable long idCliente) {
    return cuentaCorrienteService.getSaldoCuentaCorriente(idCliente);
  }

  @GetMapping("/cuentas-corriente/proveedores/{idProveedor}/saldo")
  public BigDecimal getSaldoCuentaCorrienteProveedor(@PathVariable long idProveedor) {
    return cuentaCorrienteService
        .getCuentaCorrientePorProveedor(proveedorService.getProveedorNoEliminadoPorId(idProveedor))
        .getSaldo();
  }

  @GetMapping("/cuentas-corriente/{idCuentaCorriente}/renglones")
  public Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(
      @PathVariable long idCuentaCorriente,
      @RequestParam(required = false) Integer pagina) {
    if (pagina == null || pagina < 0) pagina = 0;
    return cuentaCorrienteService.getRenglonesCuentaCorriente(idCuentaCorriente, pagina);
  }

  @PostMapping("/cuentas-corriente/clientes/reporte/criteria")
  public ResponseEntity<byte[]> getReporteCuentaCorriente(
    @RequestBody BusquedaCuentaCorrienteClienteCriteria criteria,
      @RequestParam(required = false) String formato) {
    HttpHeaders headers = new HttpHeaders();
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    switch (formato) {
      case "xlsx":
        headers.setContentType(new MediaType("application", "vnd.ms-excel"));
        headers.set("Content-Disposition", "attachment; filename=EstadoCuentaCorriente.xlsx");
        byte[] reporteXls =
            cuentaCorrienteService.getReporteCuentaCorrienteCliente(
                cuentaCorrienteService.getCuentaCorrientePorCliente(
                    clienteService.getClienteNoEliminadoPorId(criteria.getIdCliente())),
                formato);
        headers.setContentLength(reporteXls.length);
        return new ResponseEntity<>(reporteXls, headers, HttpStatus.OK);
      case "pdf":
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add("Content-Disposition", "attachment; filename=EstadoCuentaCorriente.pdf");
        byte[] reportePDF =
            cuentaCorrienteService.getReporteCuentaCorrienteCliente(
                cuentaCorrienteService.getCuentaCorrientePorCliente(
                    clienteService.getClienteNoEliminadoPorId(criteria.getIdCliente())),
                formato);
        return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
      default:
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_formato_no_valido", null, Locale.getDefault()));
    }
  }
}
