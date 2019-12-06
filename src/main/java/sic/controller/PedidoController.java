package sic.controller;

import java.util.List;

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
import sic.modelo.criteria.BusquedaPedidoCriteria;
import sic.modelo.calculos.NuevosResultadosPedido;
import sic.modelo.calculos.Resultados;
import sic.modelo.dto.DetallePedidoDTO;
import sic.modelo.dto.NuevoRenglonPedidoDTO;
import sic.service.*;

@RestController
@RequestMapping("/api/v1")
public class PedidoController {
    
    private final IPedidoService pedidoService;
    private final IAuthService authService;

  @Autowired
  public PedidoController(IPedidoService pedidoService, IAuthService authService) {
    this.pedidoService = pedidoService;
    this.authService = authService;
  }

    @GetMapping("/pedidos/{idPedido}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public Pedido getPedidoPorId(@PathVariable long idPedido) {
        return pedidoService.calcularTotalActualDePedido(pedidoService.getPedidoNoEliminadoPorId(idPedido));
    }
    
    @GetMapping("/pedidos/{idPedido}/renglones")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public List<RenglonPedido> getRenglonesDelPedido(@PathVariable long idPedido) {
        return pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglon(idPedido);
    }

  @PostMapping("/pedidos/renglones/clientes/{idCliente}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public List<RenglonPedido> calcularRenglonesPedido(
      @RequestBody List<NuevoRenglonPedidoDTO> nuevosRenglonesPedidoDTO,
      @PathVariable Long idCliente) {
    return pedidoService.calcularRenglonesPedido(nuevosRenglonesPedidoDTO, idCliente);
  }

  @PutMapping("/pedidos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
  public void actualizar(@RequestBody DetallePedidoDTO detallePedidoDTO,
                         @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    pedidoService.actualizar(detallePedidoDTO, (int) claims.get("idUsuario"));
  }

  @PostMapping("/pedidos")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Pedido guardar(@RequestBody DetallePedidoDTO detallePedidoDTO,
                        @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return pedidoService.guardar(detallePedidoDTO, (int) claims.get("idUsuario"));
  }

  @PostMapping("/pedidos/busqueda/criteria")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Page<Pedido> buscarConCriteria(
      @RequestBody BusquedaPedidoCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return pedidoService.buscarPedidos(criteria, (int) claims.get("idUsuario"));
  }

    @DeleteMapping("/pedidos/{idPedido}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public void eliminar(@PathVariable long idPedido) {
        pedidoService.eliminar(idPedido);
    }       
        
    @GetMapping("/pedidos/{idPedido}/reporte")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public ResponseEntity<byte[]> getReportePedido(@PathVariable long idPedido) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);        
        headers.add("content-disposition", "inline; filename=Pedido.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        byte[] reportePDF = pedidoService.getReportePedido(idPedido);
        return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
    }

  @PostMapping("/pedidos/calculo-pedido")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Resultados calcularResultadosPedido(@RequestBody NuevosResultadosPedido nuevosResultadosPedido) {
    return pedidoService.calcularResultadosPedido(nuevosResultadosPedido);
  }
}
