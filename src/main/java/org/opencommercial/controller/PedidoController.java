package org.opencommercial.controller;

import io.jsonwebtoken.Claims;
import org.opencommercial.aspect.AccesoRolesPermitidos;
import org.opencommercial.model.*;
import org.opencommercial.model.criteria.BusquedaPedidoCriteria;
import org.opencommercial.model.dto.NuevoRenglonPedidoDTO;
import org.opencommercial.model.dto.NuevosResultadosComprobanteDTO;
import org.opencommercial.model.dto.PedidoDTO;
import org.opencommercial.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
public class PedidoController {

  private final PedidoService pedidoService;
  private final UsuarioService usuarioService;
  private final SucursalService sucursalService;
  private final ClienteService clienteService;
  private final ReciboService reciboService;
  private final AuthService authService;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public PedidoController(PedidoService pedidoService,
                          UsuarioService usuarioService,
                          SucursalService sucursalService,
                          ClienteService clienteService,
                          ReciboService reciboService,
                          AuthService authService) {
    this.pedidoService = pedidoService;
    this.usuarioService = usuarioService;
    this.sucursalService = sucursalService;
    this.clienteService = clienteService;
    this.reciboService = reciboService;
    this.authService = authService;
  }

  @GetMapping("/api/v1/pedidos/{idPedido}")
  public Pedido getPedidoPorId(@PathVariable long idPedido) {
      return pedidoService.getPedidoNoEliminadoPorId(idPedido);
  }

  @GetMapping("/api/v1/pedidos/{idPedido}/renglones")
  public List<RenglonPedido> getRenglonesDelPedido(@PathVariable long idPedido,
                                                   @RequestParam(required = false) Boolean clonar) {
    clonar = clonar != null;
    return pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglonSegunEstadoOrClonar(idPedido, clonar);
  }

  @PostMapping("/api/v1/pedidos/renglones")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public List<RenglonPedido> calcularRenglonesPedido(
          @RequestBody List<NuevoRenglonPedidoDTO> nuevosRenglonesPedidoDTO) {
    return pedidoService.calcularRenglonesPedido(
        pedidoService.getArrayDeIdProducto(nuevosRenglonesPedidoDTO),
        pedidoService.getArrayDeCantidadesProducto(nuevosRenglonesPedidoDTO));
  }

  @PutMapping("/api/v1/pedidos")
  public void actualizar(@RequestBody PedidoDTO pedidoDTO) {
    Pedido pedido = pedidoService.getPedidoNoEliminadoPorId(pedidoDTO.getIdPedido());
    Long idSucursalOrigen = pedido.getIdSucursal();
    pedido.setSucursal(sucursalService.getSucursalPorId(pedidoDTO.getIdSucursal()));
    if (pedidoDTO.getObservaciones() != null) pedido.setObservaciones(pedidoDTO.getObservaciones());
    if (pedidoDTO.getTipoDeEnvio() != null) pedido.setTipoDeEnvio(pedidoDTO.getTipoDeEnvio());
    if (pedidoDTO.getRecargoPorcentaje() != null)
      pedido.setRecargoPorcentaje(pedidoDTO.getRecargoPorcentaje());
    if (pedidoDTO.getDescuentoPorcentaje() != null)
      pedido.setDescuentoPorcentaje(pedidoDTO.getDescuentoPorcentaje());
    List<RenglonPedido> renglonesAnteriores = new ArrayList<>(pedido.getRenglones());
    pedido.getRenglones().clear();
    pedido
        .getRenglones()
        .addAll(
            pedidoService.calcularRenglonesPedido(
                pedidoService.getArrayDeIdProducto(pedidoDTO.getRenglones()),
                pedidoService.getArrayDeCantidadesProducto(pedidoDTO.getRenglones())));
    pedidoService.actualizar(
        pedido,
        renglonesAnteriores,
        idSucursalOrigen,
        reciboService.construirRecibos(
            pedidoDTO.getIdsFormaDePago(),
            pedido.getSucursal().getIdSucursal(),
            pedido.getCliente(),
            pedido.getUsuario(),
            pedidoDTO.getMontos(),
            LocalDateTime.now()));
  }

  @PostMapping("/api/v1/pedidos")
  public Pedido guardar(@RequestBody PedidoDTO pedidoDTO,
                        @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Pedido pedido = new Pedido();
    pedido.setObservaciones(pedidoDTO.getObservaciones());
    pedido.setRecargoPorcentaje(pedidoDTO.getRecargoPorcentaje());
    pedido.setDescuentoPorcentaje(pedidoDTO.getDescuentoPorcentaje());
    Sucursal sucursalDePedido;
    sucursalDePedido = sucursalService.getSucursalPorId(pedidoDTO.getIdSucursal());
    pedido.setSucursal(sucursalDePedido);
    long idUsuario = claims.get(CLAIM_ID_USUARIO, Long.class);
    pedido.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuario));
    pedido.setCliente(clienteService.getClienteNoEliminadoPorId(pedidoDTO.getIdCliente()));
    if (pedidoDTO.getTipoDeEnvio() != null) pedido.setTipoDeEnvio(pedidoDTO.getTipoDeEnvio());
    pedido.setRenglones(
        pedidoService.calcularRenglonesPedido(
            pedidoService.getArrayDeIdProducto(pedidoDTO.getRenglones()),
            pedidoService.getArrayDeCantidadesProducto(pedidoDTO.getRenglones())));
    return pedidoService.guardar(
        pedido,
        reciboService.construirRecibos(
            pedidoDTO.getIdsFormaDePago(),
            sucursalDePedido.getIdSucursal(),
            pedido.getCliente(),
            pedido.getUsuario(),
            pedidoDTO.getMontos(),
            LocalDateTime.now()));
  }

  @PostMapping("/api/v1/pedidos/busqueda/criteria")
  public Page<Pedido> buscarConCriteria(@RequestBody BusquedaPedidoCriteria criteria,
                                        @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return pedidoService.buscarPedidos(criteria, claims.get(CLAIM_ID_USUARIO, Long.class));
  }

  @PutMapping("/api/v1/pedidos/{idPedido}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public void cancelar(@PathVariable long idPedido) {
    pedidoService.cancelar(pedidoService.getPedidoNoEliminadoPorId(idPedido));
  }

  @GetMapping("/api/v1/pedidos/{idPedido}/reporte")
  public ResponseEntity<byte[]> getReportePedido(@PathVariable long idPedido) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.add("content-disposition", "inline; filename=Pedido.pdf");
      headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
      byte[] reportePDF = pedidoService.getReportePedido(idPedido);
      return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
  }

  @PostMapping("/api/v1/pedidos/calculo-pedido")
  public Resultados calcularResultadosPedido(
          @RequestBody NuevosResultadosComprobanteDTO nuevosResultadosComprobanteDTO) {
    return pedidoService.calcularResultadosPedido(nuevosResultadosComprobanteDTO);
  }
}
