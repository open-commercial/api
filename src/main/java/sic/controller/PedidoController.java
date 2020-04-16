package sic.controller;

import java.math.BigDecimal;
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
    private final IAuthService authService;
    private final MessageSource messageSource;
    private static final Long ID_SUCURSAL_DEFAULT = 1L;

  @Autowired
  public PedidoController(
      IPedidoService pedidoService,
      IUsuarioService usuarioService,
      ISucursalService sucursalService,
      IClienteService clienteService,
      MessageSource messageSource,
      IAuthService authService) {
    this.pedidoService = pedidoService;
    this.usuarioService = usuarioService;
    this.sucursalService = sucursalService;
    this.clienteService = clienteService;
    this.messageSource = messageSource;
    this.authService = authService;
  }

    @GetMapping("/pedidos/{idPedido}")
    public Pedido getPedidoPorId(@PathVariable long idPedido) {
        return pedidoService.calcularTotalActualDePedido(pedidoService.getPedidoNoEliminadoPorId(idPedido));
    }
    
    @GetMapping("/pedidos/{idPedido}/renglones")
    public List<RenglonPedido> getRenglonesDelPedido(@PathVariable long idPedido) {
        return pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglonSegunEstado(idPedido);
    }

  @PostMapping("/pedidos/renglones/clientes/{idCliente}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public List<RenglonPedido> calcularRenglonesPedido(
      @RequestBody List<NuevoRenglonPedidoDTO> nuevosRenglonesPedidoDTO) {
    return pedidoService.calcularRenglonesPedido(
        this.getArrayDeIdProducto(nuevosRenglonesPedidoDTO),
        this.getArrayDeCantidadesProducto(nuevosRenglonesPedidoDTO));
  }

  @PutMapping("/pedidos")
  public void actualizar(
      @RequestBody PedidoDTO pedidoDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Pedido pedido = pedidoService.getPedidoNoEliminadoPorId(pedidoDTO.getIdPedido());
    long idUsuario = (int) claims.get("idUsuario");
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
                this.getArrayDeIdProducto(pedidoDTO.getRenglones()),
                this.getArrayDeCantidadesProducto(pedidoDTO.getRenglones())));
    pedidoService.actualizar(pedido, renglonesAnteriores);
  }

  @PostMapping("/pedidos")
  public Pedido guardar(@RequestBody PedidoDTO pedidoDTO,
                        @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Pedido pedido = new Pedido();
    pedido.setObservaciones(pedidoDTO.getObservaciones());
    pedido.setRecargoPorcentaje(pedidoDTO.getRecargoPorcentaje());
    pedido.setDescuentoPorcentaje(pedidoDTO.getDescuentoPorcentaje());
    if (pedidoDTO.getIdSucursal() == null) {
      if (!pedidoDTO.getTipoDeEnvio().equals(TipoDeEnvio.RETIRO_EN_SUCURSAL)) {
        pedido.setSucursal(sucursalService.getSucursalPorId(ID_SUCURSAL_DEFAULT));
      } else {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_pedido_retiro_sucursal_no_seleccionada", null, Locale.getDefault()));
      }
    } else {
      pedido.setSucursal(sucursalService.getSucursalPorId(pedidoDTO.getIdSucursal()));
    }
    long idUsuario = (int) claims.get("idUsuario");
    pedido.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuario));
    pedido.setCliente(clienteService.getClienteNoEliminadoPorId(pedidoDTO.getIdCliente()));
    if (pedidoDTO.getTipoDeEnvio() != null) pedido.setTipoDeEnvio(pedidoDTO.getTipoDeEnvio());
    pedido.setRenglones(
        pedidoService.calcularRenglonesPedido(
            this.getArrayDeIdProducto(pedidoDTO.getRenglones()),
            this.getArrayDeCantidadesProducto(pedidoDTO.getRenglones())));
    return pedidoService.guardar(pedido);
  }

  @PostMapping("/pedidos/busqueda/criteria")
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

  private long[] getArrayDeIdProducto(List<NuevoRenglonPedidoDTO> nuevosRenglones) {
    long[] idProductoItem = new long[nuevosRenglones.size()];
    for (int i = 0; i < nuevosRenglones.size(); ++i) {
      idProductoItem[i] = nuevosRenglones.get(i).getIdProductoItem();
    }
    return idProductoItem;
  }

  private BigDecimal[] getArrayDeCantidadesProducto(List<NuevoRenglonPedidoDTO> nuevosRenglones) {
    BigDecimal[] cantidades = new BigDecimal[nuevosRenglones.size()];
    for (int i = 0; i < nuevosRenglones.size(); ++i) {
      cantidades[i] = nuevosRenglones.get(i).getCantidad();
    }
    return cantidades;
  }
}
