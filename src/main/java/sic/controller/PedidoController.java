package sic.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaPedidoCriteria;
import sic.modelo.dto.NuevosResultadosComprobanteDTO;
import sic.modelo.Resultados;
import sic.modelo.dto.PedidoDTO;
import sic.modelo.dto.NuevoRenglonPedidoDTO;
import sic.service.*;

@RestController
@RequestMapping("/api/v1")
public class PedidoController {

    private final IPedidoService pedidoService;
    private final IUsuarioService usuarioService;
    private final ISucursalService sucursalService;
    private final IClienteService clienteService;
    private final IReciboService reciboService;
    private final IAuthService authService;
    private final MessageSource messageSource;
    private static final String ID_USUARIO = "idUsuario";

  @Autowired
  public PedidoController(
      IPedidoService pedidoService,
      IUsuarioService usuarioService,
      ISucursalService sucursalService,
      IClienteService clienteService,
      IReciboService reciboService,
      MessageSource messageSource,
      IAuthService authService) {
    this.pedidoService = pedidoService;
    this.usuarioService = usuarioService;
    this.sucursalService = sucursalService;
    this.clienteService = clienteService;
    this.reciboService = reciboService;
    this.messageSource = messageSource;
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
      @RequestBody List<NuevoRenglonPedidoDTO> nuevosRenglonesPedidoDTO) {
    return pedidoService.calcularRenglonesPedido(
        pedidoService.getArrayDeIdProducto(nuevosRenglonesPedidoDTO),
        pedidoService.getArrayDeCantidadesProducto(nuevosRenglonesPedidoDTO));
  }

  @PutMapping("/pedidos")
  public void actualizar(
      @RequestBody PedidoDTO pedidoDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelJWT(authorizationHeader);
    Pedido pedido = pedidoService.getPedidoNoEliminadoPorId(pedidoDTO.getIdPedido());
    Long idSucursalOrigen = pedido.getIdSucursal();
    long idUsuario = (int) claims.get(ID_USUARIO);
    pedido.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuario));
    if (pedidoDTO.getIdSucursal() != null)
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

  @PostMapping("/pedidos")
  public Pedido guardar(@RequestBody PedidoDTO pedidoDTO,
                        @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelJWT(authorizationHeader);
    Pedido pedido = new Pedido();
    pedido.setObservaciones(pedidoDTO.getObservaciones());
    pedido.setRecargoPorcentaje(pedidoDTO.getRecargoPorcentaje());
    pedido.setDescuentoPorcentaje(pedidoDTO.getDescuentoPorcentaje());
    Sucursal sucursalDePedido;
    if (pedidoDTO.getIdSucursal() == null) {
      if (!pedidoDTO.getTipoDeEnvio().equals(TipoDeEnvio.RETIRO_EN_SUCURSAL)) {
        sucursalDePedido = sucursalService.getSucursalPredeterminada();
        pedido.setSucursal(sucursalDePedido);
      } else {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_pedido_retiro_sucursal_no_seleccionada", null, Locale.getDefault()));
      }
    } else {
      sucursalDePedido = sucursalService.getSucursalPorId(pedidoDTO.getIdSucursal());
      pedido.setSucursal(sucursalDePedido);
    }
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
    Claims claims = authService.getClaimsDelJWT(authorizationHeader);
    return pedidoService.buscarPedidos(criteria, (int) claims.get(ID_USUARIO));
  }

  @PutMapping("/pedidos/{idPedido}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public void cancelar(@PathVariable long idPedido) {
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
}
