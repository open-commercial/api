package sic.controller;

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
import sic.modelo.calculos.NuevosResultadosPedidoDTO;
import sic.modelo.calculos.Resultados;
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
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    nuevosRenglonesPedidoDTO.forEach(
        nuevoRenglonesPedidoDTO ->
            renglonesPedido.add(
                pedidoService.calcularRenglonPedido(
                    nuevoRenglonesPedidoDTO.getIdProductoItem(),
                    nuevoRenglonesPedidoDTO.getCantidad(),
                    clienteService.getClienteNoEliminadoPorId(idCliente))));
    return renglonesPedido;
  }

  @PutMapping("/pedidos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
  public void actualizar(@RequestBody PedidoDTO pedidoDTO,
                         @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Pedido pedido = pedidoService.getPedidoNoEliminadoPorId(pedidoDTO.getIdPedido());
    long idUsuario = (int) claims.get("idUsuario");
    pedido.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuario));
    if (pedidoDTO.getIdSucursal() != null)
      pedido.setSucursal(sucursalService.getSucursalPorId(pedidoDTO.getIdSucursal()));
    if (pedidoDTO.getObservaciones() != null)
      pedido.setObservaciones(pedidoDTO.getObservaciones());
    if (pedidoDTO.getTipoDeEnvio() != null)
      pedido.setTipoDeEnvio(pedidoDTO.getTipoDeEnvio());
    pedido.getRenglones().clear();
    pedido.getRenglones().addAll(this.calcularRenglonesPedido(pedidoDTO.getRenglones(), pedido.getCliente().getIdCliente()));
    pedidoService.actualizar(pedido);
  }

  @PostMapping("/pedidos")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
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
    if (pedidoDTO.getTipoDeEnvio() != null)
      pedido.setTipoDeEnvio(pedidoDTO.getTipoDeEnvio());
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    pedidoDTO
        .getRenglones()
        .forEach(
            nuevoRenglonesPedidoDTO ->
                renglonesPedido.add(
                    pedidoService.calcularRenglonPedido(
                        nuevoRenglonesPedidoDTO.getIdProductoItem(),
                        nuevoRenglonesPedidoDTO.getCantidad(),
                        clienteService.getClienteNoEliminadoPorId(pedidoDTO.getIdCliente()))));
    pedido.setRenglones(renglonesPedido);
    return pedidoService.guardar(pedido);
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
  public Resultados calcularResultadosPedido(@RequestBody NuevosResultadosPedidoDTO nuevosResultadosPedidoDTO) {
    return pedidoService.calcularResultadosPedido(nuevosResultadosPedidoDTO);
  }
}
