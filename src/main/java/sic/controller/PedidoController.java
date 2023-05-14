package sic.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import sic.modelo.dto.*;
import sic.modelo.Resultados;
import sic.service.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
public class PedidoController {

    private final IPedidoService pedidoService;
    private final IUsuarioService usuarioService;
    private final ISucursalService sucursalService;
    private final IClienteService clienteService;
    private final IReciboService reciboService;
    private final IAuthService authService;
    private static final String ID_USUARIO = "idUsuario";

  @Autowired
  public PedidoController(
      IPedidoService pedidoService,
      IUsuarioService usuarioService,
      ISucursalService sucursalService,
      IClienteService clienteService,
      IReciboService reciboService,
      IAuthService authService) {
    this.pedidoService = pedidoService;
    this.usuarioService = usuarioService;
    this.sucursalService = sucursalService;
    this.clienteService = clienteService;
    this.reciboService = reciboService;
    this.authService = authService;
  }

    @GetMapping("/pedidos/{idPedido}")
    public Pedido getPedidoPorId(@PathVariable long idPedido) {
        return pedidoService.getPedidoNoEliminadoPorId(idPedido);
    }

    @GetMapping("/pedidos/{idPedido}/renglones")
    public List<RenglonPedido> getRenglonesDelPedido(@PathVariable long idPedido,
                                                     @RequestParam(required = false) boolean clonar) {
        return pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglonSegunEstadoOrClonar(idPedido, clonar);
    }

  @PostMapping("/pedidos/renglones/clientes/{idCliente}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public List<RenglonPedido> calcularRenglonesPedido(
      @RequestBody List<CantidadProductoDTO> nuevosRenglonesPedidoDTO) {
    return pedidoService.calcularRenglonesPedido(
        pedidoService.getArrayDeIdProducto(nuevosRenglonesPedidoDTO),
        pedidoService.getArrayDeCantidadesProducto(nuevosRenglonesPedidoDTO));
  }

  @PutMapping("/pedidos")
  public void actualizar(@RequestBody PedidoDTO pedidoDTO, HttpServletRequest request) {
    Pedido pedido = pedidoService.getPedidoNoEliminadoPorId(pedidoDTO.getIdPedido());
    Long idSucursalOrigen = pedido.getIdSucursal();
    pedido.setSucursal(sucursalService.getSucursalPorId(pedidoDTO.getIdSucursal()));
    if (pedidoDTO.getObservaciones() != null) pedido.setObservaciones(pedidoDTO.getObservaciones());
    if (pedidoDTO.getTipoDeEnvio() != null) pedido.setTipoDeEnvio(pedidoDTO.getTipoDeEnvio());
    if (pedidoDTO.getRecargoPorcentaje() != null)
      pedido.setRecargoPorcentaje(pedidoDTO.getRecargoPorcentaje());
    if (pedidoDTO.getDescuentoPorcentaje() != null)
      pedido.setDescuentoPorcentaje(pedidoDTO.getDescuentoPorcentaje());
    List<CantidadProductoDTO> renglonesAnteriores = new ArrayList<>();
    pedido.getRenglones().forEach(renglonPedido -> renglonesAnteriores.add(CantidadProductoDTO.builder()
            .idProductoItem(renglonPedido.getIdProductoItem()).cantidad(renglonPedido.getCantidad()).build()));
    pedido.setRenglones(this.pedidoService.actualizarRenglonesPedido(pedido.getRenglones(), pedidoDTO.getRenglones()));
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

  @PostMapping("/pedidos")
  public Pedido guardar(@RequestBody PedidoDTO pedidoDTO,
                        @RequestHeader("Authorization") String authorizationHeader,
                        HttpServletRequest request) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Pedido pedido = new Pedido();
    pedido.setObservaciones(pedidoDTO.getObservaciones());
    pedido.setRecargoPorcentaje(pedidoDTO.getRecargoPorcentaje());
    pedido.setDescuentoPorcentaje(pedidoDTO.getDescuentoPorcentaje());
    Sucursal sucursalDePedido;
    sucursalDePedido = sucursalService.getSucursalPorId(pedidoDTO.getIdSucursal());
    pedido.setSucursal(sucursalDePedido);
    long idUsuario = (int) claims.get(ID_USUARIO);
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

  @PostMapping("/pedidos/busqueda/criteria")
  public Page<Pedido> buscarConCriteria(
      @RequestBody BusquedaPedidoCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return pedidoService.buscarPedidos(criteria, (int) claims.get(ID_USUARIO));
  }

  @PutMapping("/pedidos/{idPedido}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public void cancelar(@PathVariable long idPedido, HttpServletRequest request) {
    pedidoService.cancelar(pedidoService.getPedidoNoEliminadoPorId(idPedido));
  }

    @GetMapping("/pedidos/{idPedido}/reporte")
    public ResponseEntity<byte[]> getReportePedido(@PathVariable long idPedido) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);        
        headers.add("content-disposition", "inline; filename=Pedido.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        byte[] reportePDF = pedidoService.getReportePedido(idPedido);
        return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
    }

  @PostMapping("/pedidos/calculo-pedido")
  public Resultados calcularResultadosPedido(@RequestBody NuevosResultadosComprobanteDTO nuevosResultadosComprobanteDTO) {
    return pedidoService.calcularResultadosPedido(nuevosResultadosComprobanteDTO);
  }

  @GetMapping("/pedidos/{idPedido}/cambios")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<CommitDTO> getCambios(@PathVariable long idPedido) {
    return pedidoService.getCambiosPedido(idPedido);
  }

  @GetMapping("/pedidos/{idPedido}/renglones/cambios")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Map<String, List<CommitDTO>> getCambiosRenglones(@PathVariable long idPedido) {
    return pedidoService.getCambiosRenglonesPedido(idPedido);
  }
}
