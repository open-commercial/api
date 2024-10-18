package sic.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaNotaCriteria;
import sic.modelo.dto.NuevaNotaCreditoDeFacturaDTO;
import sic.modelo.dto.NuevaNotaCreditoSinFacturaDTO;
import sic.modelo.dto.NuevaNotaDebitoDeReciboDTO;
import sic.modelo.dto.NuevaNotaDebitoSinReciboDTO;
import sic.service.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class NotaController {

  private final NotaService notaService;
  private final ReciboService reciboService;
  private final SucursalService sucursalService;
  private final UsuarioService usuarioService;
  private final AuthService authService;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public NotaController(NotaService notaService,
                        ReciboService reciboService,
                        SucursalService sucursalService,
                        UsuarioService usuarioService,
                        AuthService authService) {
    this.notaService = notaService;
    this.reciboService = reciboService;
    this.sucursalService = sucursalService;
    this.usuarioService = usuarioService;
    this.authService = authService;
  }

  @GetMapping("/api/v1/notas/{idNota}")
  public Nota getNota(@PathVariable long idNota) {
    return notaService.getNotaNoEliminadaPorId(idNota);
  }

  @DeleteMapping("/api/v1/notas/{idNota}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminarNota(@PathVariable long idNota) {
    notaService.eliminarNota(idNota);
  }

  @GetMapping("/api/v1/notas/credito/tipos/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public TipoDeComprobante[] getTipoNotaCreditoSucursal(@PathVariable long idSucursal) {
    return notaService.getTiposNotaCredito(sucursalService.getSucursalPorId(idSucursal));
  }

  @GetMapping("/api/v1/notas/debito/tipos/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public TipoDeComprobante[] getTipoNotaDebitoSucursal(@PathVariable long idSucursal) {
    return notaService.getTiposNotaDebito(sucursalService.getSucursalPorId(idSucursal));
  }

  @GetMapping("/api/v1/notas/{idNota}/facturas")
  public Factura getFacturaNotaCredito(@PathVariable long idNota) {
    return notaService.getFacturaDeLaNotaCredito(idNota);
  }

  @GetMapping("/api/v1/notas/debito/recibo/{idRecibo}/existe")
  public boolean existeNotaDebitoRecibo(@PathVariable long idRecibo) {
    return notaService.existsNotaDebitoPorRecibo(reciboService.getReciboNoEliminadoPorId(idRecibo));
  }

  @GetMapping("/api/v1/notas/clientes/tipos/credito")
  public List<TipoDeComprobante> getTipoNotaCreditoCliente(@RequestParam long idCliente,
                                                           @RequestParam long idSucursal) {
    return notaService.getTipoNotaCreditoCliente(idCliente, idSucursal);
  }

  @GetMapping("/api/v1/notas/clientes/tipos/debito")
  public List<TipoDeComprobante> getTipoNotaDebitoCliente(@RequestParam long idCliente,
                                                          @RequestParam long idSucursal) {
    return notaService.getTipoNotaDebitoCliente(idCliente, idSucursal);
  }

  @GetMapping("/api/v1/notas/proveedores/tipos/credito")
  public List<TipoDeComprobante> getTipoNotaCreditoProveedor(@RequestParam long idProveedor,
                                                             @RequestParam long idSucursal) {
    return notaService.getTipoNotaCreditoProveedor(idProveedor, idSucursal);
  }

  @GetMapping("/api/v1/notas/proveedores/tipos/debito")
  public List<TipoDeComprobante> getTipoNotaDebitoProveedor(@RequestParam long idProveedor,
                                                            @RequestParam long idSucursal) {
    return notaService.getTipoNotaDebitoProveedor(idProveedor, idSucursal);
  }

  @GetMapping("/api/v1/notas/renglones/credito/{idNotaCredito}")
  public List<RenglonNotaCredito> getRenglonesDeNotaCreditoCliente(@PathVariable long idNotaCredito) {
    return notaService.getRenglonesDeNotaCredito(idNotaCredito);
  }

  @GetMapping("/api/v1/notas/renglones/debito/{idNotaDebito}")
  public List<RenglonNotaDebito> getRenglonesDeNotaDebitoCliente(@PathVariable long idNotaDebito) {
    return notaService.getRenglonesDeNotaDebito(idNotaDebito);
  }

  @PostMapping("/api/v1/notas/credito/calculos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public NotaCredito calcularNotaCreditoConFactura(
          @RequestBody NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDeFacturaDTO,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.calcularNotaCreditoConFactura(
            nuevaNotaCreditoDeFacturaDTO,
            usuarioService.getUsuarioNoEliminadoPorId(claims.get(CLAIM_ID_USUARIO, Long.class)));
  }

  @PostMapping("/api/v1/notas/credito/calculos-sin-factura")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public NotaCredito calcularNotaCreditoSinFactura(
          @RequestBody NuevaNotaCreditoSinFacturaDTO nuevaNotaCreditoSinFacturaDTO,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.calcularNotaCreditoSinFactura(
            nuevaNotaCreditoSinFacturaDTO,
            usuarioService.getUsuarioNoEliminadoPorId(claims.get(CLAIM_ID_USUARIO, Long.class)));
  }

  @PostMapping("/api/v1/notas/debito/calculos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public NotaDebito calcularNotaDebitoDeRecibo(
          @RequestBody NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.calcularNotaDebitoConRecibo(
            nuevaNotaDebitoDeReciboDTO,
            usuarioService.getUsuarioNoEliminadoPorId(claims.get(CLAIM_ID_USUARIO, Long.class)));
  }

  @PostMapping("/api/v1/notas/debito/calculos-sin-recibo")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public NotaDebito calcularNotaDebitoSinRecibo(
          @RequestBody NuevaNotaDebitoSinReciboDTO nuevaNotaDebitoSinReciboDTO,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.calcularNotaDebitoSinRecibo(
            nuevaNotaDebitoSinReciboDTO,
            usuarioService.getUsuarioNoEliminadoPorId(claims.get(CLAIM_ID_USUARIO, Long.class)));
  }

  @PostMapping("/api/v1/notas/credito/factura")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public NotaCredito guardarNotaCreditoDeFactura(
          @RequestBody NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDeFacturaDTO,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.guardarNotaCredito(
            notaService.calcularNotaCreditoConFactura(
                    nuevaNotaCreditoDeFacturaDTO,
                    usuarioService.getUsuarioNoEliminadoPorId(claims.get(CLAIM_ID_USUARIO, Long.class))));
  }

  @PostMapping("/api/v1/notas/credito/sin-factura")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public NotaCredito guardarNotaCreditoSinFactura(
          @RequestBody NuevaNotaCreditoSinFacturaDTO nuevaNotaCreditoSinFacturaDTO,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.guardarNotaCredito(
            notaService.calcularNotaCreditoSinFactura(
                    nuevaNotaCreditoSinFacturaDTO,
                    usuarioService.getUsuarioNoEliminadoPorId(claims.get(CLAIM_ID_USUARIO, Long.class))));
  }

  @PostMapping("/api/v1/notas/debito")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public NotaDebito guardarNotaDebitoDeRecibo(
          @RequestBody NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.guardarNotaDebito(
            notaService.calcularNotaDebitoConRecibo(
                    nuevaNotaDebitoDeReciboDTO,
                    usuarioService.getUsuarioNoEliminadoPorId(claims.get(CLAIM_ID_USUARIO, Long.class))));
  }

  @PostMapping("/api/v1/notas/debito/sin-recibo")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public NotaDebito guardarNotaDebitoSinRecibo(
          @RequestBody NuevaNotaDebitoSinReciboDTO nuevaNotaDebitoSinReciboDTO,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.guardarNotaDebito(notaService.calcularNotaDebitoSinRecibo(
            nuevaNotaDebitoSinReciboDTO,
            usuarioService.getUsuarioNoEliminadoPorId(claims.get(CLAIM_ID_USUARIO, Long.class))));
  }

  @GetMapping("/api/v1/notas/{idNota}/reporte")
  public ResponseEntity<byte[]> getReporteNota(@PathVariable long idNota) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    Nota nota = notaService.getNotaNoEliminadaPorId(idNota);
    String fileName = (nota instanceof NotaCredito) ? "NotaCredito.pdf" : "NotaDebito.pdf";
    headers.add("content-disposition", "inline; filename=" + fileName);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    byte[] reportePDF = notaService.getReporteNota(nota);
    return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
  }

  @GetMapping("/api/v1/notas/debito/total")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal calcularTotalDebito(BigDecimal subTotalBruto,
                                        BigDecimal iva21Neto,
                                        BigDecimal montoNoGravado) {
    return notaService.calcularTotalDebito(subTotalBruto, iva21Neto, montoNoGravado);
  }

  @PostMapping("/api/v1/notas/credito/busqueda/criteria")
  public Page<NotaCredito> buscarNotasCredito(
          @RequestBody BusquedaNotaCriteria busquedaNotaCriteria,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.buscarNotasCredito(busquedaNotaCriteria, claims.get(CLAIM_ID_USUARIO, Long.class));
  }

  @PostMapping("/api/v1/notas/debito/busqueda/criteria")
  public Page<NotaDebito> buscarNotasDebito(
          @RequestBody BusquedaNotaCriteria busquedaNotaCriteria,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.buscarNotasDebito(busquedaNotaCriteria, claims.get(CLAIM_ID_USUARIO, Long.class));
  }

  @PostMapping("/api/v1/notas/total-credito/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalNotasCredito(
          @RequestBody BusquedaNotaCriteria busquedaNotaCriteria,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.calcularTotalCredito(busquedaNotaCriteria, claims.get(CLAIM_ID_USUARIO, Long.class));
  }

  @PostMapping("/api/v1/notas/total-debito/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalNotasDebito(
          @RequestBody BusquedaNotaCriteria busquedaNotaCriteria,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.calcularTotalDebito(busquedaNotaCriteria, claims.get(CLAIM_ID_USUARIO, Long.class));
  }

  @PostMapping("/api/v1/notas/total-iva-credito/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalIvaCredito(
          @RequestBody BusquedaNotaCriteria busquedaNotaCriteria,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.calcularTotalIVACredito(busquedaNotaCriteria, claims.get(CLAIM_ID_USUARIO, Long.class));
  }

  @PostMapping("/api/v1/notas/total-iva-debito/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalIvaDebito(
          @RequestBody BusquedaNotaCriteria busquedaNotaCriteria,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return notaService.calcularTotalIVADebito(busquedaNotaCriteria, claims.get(CLAIM_ID_USUARIO, Long.class));
  }
}
