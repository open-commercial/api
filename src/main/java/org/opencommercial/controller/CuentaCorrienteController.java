package org.opencommercial.controller;

import io.jsonwebtoken.Claims;
import org.opencommercial.aspect.AccesoRolesPermitidos;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.model.CuentaCorrienteCliente;
import org.opencommercial.model.CuentaCorrienteProveedor;
import org.opencommercial.model.RenglonCuentaCorriente;
import org.opencommercial.model.Rol;
import org.opencommercial.model.criteria.BusquedaCuentaCorrienteClienteCriteria;
import org.opencommercial.model.criteria.BusquedaCuentaCorrienteProveedorCriteria;
import org.opencommercial.service.AuthService;
import org.opencommercial.service.ClienteService;
import org.opencommercial.service.CuentaCorrienteService;
import org.opencommercial.service.ProveedorService;
import org.opencommercial.util.FormatoReporte;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Locale;

@RestController
public class CuentaCorrienteController {

  private final CuentaCorrienteService cuentaCorrienteService;
  private final ProveedorService proveedorService;
  private final ClienteService clienteService;
  private final AuthService authService;
  private final MessageSource messageSource;
  private static final String CLAIM_ID_USUARIO = "idUsuario";
  private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";

  @Autowired
  public CuentaCorrienteController(CuentaCorrienteService cuentaCorrienteService,
                                   ProveedorService proveedorService,
                                   ClienteService clienteService,
                                   AuthService authService,
                                   MessageSource messageSource) {
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.clienteService = clienteService;
    this.proveedorService = proveedorService;
    this.authService = authService;
    this.messageSource = messageSource;
  }

  @PostMapping("/api/v1/cuentas-corriente/clientes/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public Page<CuentaCorrienteCliente> buscarCuentasCorrienteCliente(
      @RequestBody BusquedaCuentaCorrienteClienteCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return cuentaCorrienteService.buscarCuentaCorrienteCliente(criteria, claims.get(CLAIM_ID_USUARIO, Long.class));
  }

  @PostMapping("/api/v1/cuentas-corriente/proveedores/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Page<CuentaCorrienteProveedor> buscarCuentasCorrienteProveedor(
      @RequestBody BusquedaCuentaCorrienteProveedorCriteria criteria) {
    return cuentaCorrienteService.buscarCuentaCorrienteProveedor(criteria);
  }

  @GetMapping("/api/v1/cuentas-corriente/clientes/{idCliente}")
  public CuentaCorrienteCliente getCuentaCorrientePorCliente(@PathVariable Long idCliente) {
    return cuentaCorrienteService.getCuentaCorrientePorCliente(
        clienteService.getClienteNoEliminadoPorId(idCliente));
  }

  @GetMapping("/api/v1/cuentas-corriente/clientes/predeterminado")
  public CuentaCorrienteCliente getCuentaCorrienteClientePredeterminado() {
    return cuentaCorrienteService.getCuentaCorrientePorCliente(
        clienteService.getClientePredeterminado());
  }

  @GetMapping("/api/v1/cuentas-corriente/proveedores/{idProveedor}")
  public CuentaCorrienteProveedor getCuentaCorrientePorProveedor(@PathVariable Long idProveedor) {
    return cuentaCorrienteService.getCuentaCorrientePorProveedor(
        proveedorService.getProveedorNoEliminadoPorId(idProveedor));
  }

  @GetMapping("/api/v1/cuentas-corriente/clientes/{idCliente}/saldo")
  public BigDecimal getSaldoCuentaCorrienteCliente(@PathVariable long idCliente) {
    return cuentaCorrienteService.getSaldoCuentaCorriente(idCliente);
  }

  @GetMapping("/api/v1/cuentas-corriente/proveedores/{idProveedor}/saldo")
  public BigDecimal getSaldoCuentaCorrienteProveedor(@PathVariable long idProveedor) {
    return cuentaCorrienteService
        .getCuentaCorrientePorProveedor(proveedorService.getProveedorNoEliminadoPorId(idProveedor))
        .getSaldo();
  }

  @GetMapping("/api/v1/cuentas-corriente/{idCuentaCorriente}/renglones")
  public Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(
          @PathVariable long idCuentaCorriente,
          @RequestParam(required = false) Integer pagina) {
    if (pagina == null || pagina < 0) pagina = 0;
    return cuentaCorrienteService.getRenglonesCuentaCorriente(idCuentaCorriente, pagina);
  }

  @PostMapping("/api/v1/cuentas-corriente/clientes/reporte/criteria")
  public ResponseEntity<byte[]> getReporteCuentaCorriente(
      @RequestBody BusquedaCuentaCorrienteClienteCriteria criteria,
      @RequestParam(required = false) String formato) {
    HttpHeaders headers = new HttpHeaders();
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    switch (formato) {
      case "xlsx" -> {
        headers.setContentType(new MediaType("application", "vnd.ms-excel"));
        headers.set(CONTENT_DISPOSITION_HEADER, "attachment; filename=EstadoCuentaCorriente.xlsx");
        byte[] reporteXls =
                cuentaCorrienteService.getReporteCuentaCorrienteCliente(
                        cuentaCorrienteService.getCuentaCorrientePorCliente(
                                clienteService.getClienteNoEliminadoPorId(criteria.getIdCliente())),
                        FormatoReporte.XLSX);
        headers.setContentLength(reporteXls.length);
        return new ResponseEntity<>(reporteXls, headers, HttpStatus.OK);
      }
      case "pdf" -> {
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(CONTENT_DISPOSITION_HEADER, "attachment; filename=EstadoCuentaCorriente.pdf");
        byte[] reportePDF =
                cuentaCorrienteService.getReporteCuentaCorrienteCliente(
                        cuentaCorrienteService.getCuentaCorrientePorCliente(
                                clienteService.getClienteNoEliminadoPorId(criteria.getIdCliente())),
                        FormatoReporte.PDF);
        return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
      }
      default -> throw new BusinessServiceException(messageSource.getMessage(
              "mensaje_formato_no_valido", null, Locale.getDefault()));
    }
  }

  @PostMapping("/api/v1/cuentas-corriente/lista-clientes/reporte/criteria")
  public ResponseEntity<byte[]> getReporteListaDeCuentasCorrienteClientePorCriteria(
      @RequestBody BusquedaCuentaCorrienteClienteCriteria criteria,
      @RequestParam(required = false) String formato,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    HttpHeaders headers = new HttpHeaders();
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    return switch (formato) {
      case "xlsx" ->
        this.getReporteListaDeCuentasCorrienteClienteEnXlsx(headers, criteria, claims);
      case "pdf" ->
        this.getReporteListaDeCuentasCorrienteClienteEnPdf(headers, criteria, claims);
      default -> throw new BusinessServiceException(
              messageSource.getMessage("mensaje_formato_no_valido", null, Locale.getDefault()));
    };
  }

  private ResponseEntity<byte[]> getReporteListaDeCuentasCorrienteClienteEnXlsx(
          HttpHeaders headers, BusquedaCuentaCorrienteClienteCriteria criteria, Claims claims) {
    headers.setContentType(new MediaType("application", "vnd.ms-excel"));
    headers.set(CONTENT_DISPOSITION_HEADER, "attachment; filename=ListaClientes.xlsx");
    byte[] reporteXls =
            cuentaCorrienteService.getReporteListaDeCuentasCorrienteClientePorCriteria(
                    criteria, claims.get(CLAIM_ID_USUARIO, Long.class), FormatoReporte.XLSX);
    headers.setContentLength(reporteXls.length);
    return new ResponseEntity<>(reporteXls, headers, HttpStatus.OK);
  }

  private ResponseEntity<byte[]> getReporteListaDeCuentasCorrienteClienteEnPdf(
          HttpHeaders headers, BusquedaCuentaCorrienteClienteCriteria criteria, Claims claims) {
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.add(CONTENT_DISPOSITION_HEADER, "attachment; filename=ListaClientes.pdf");
    byte[] reportePDF =
            cuentaCorrienteService.getReporteListaDeCuentasCorrienteClientePorCriteria(
                    criteria, claims.get(CLAIM_ID_USUARIO, Long.class), FormatoReporte.PDF);
    return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
  }
}
