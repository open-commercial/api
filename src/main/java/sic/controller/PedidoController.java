package sic.controller;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
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
import sic.modelo.calculos.NuevosResultadosPedido;
import sic.modelo.calculos.Resultados;
import sic.modelo.dto.NuevoPedidoDTO;
import sic.modelo.dto.NuevoRenglonPedidoDTO;
import sic.modelo.dto.PedidoDTO;
import sic.service.*;

@RestController
@RequestMapping("/api/v1")
public class PedidoController {
    
    private final IPedidoService pedidoService;
    private final ISucursalService sucursalService;
    private final IUsuarioService usuarioService;
    private final IClienteService clienteService;
    private final IAuthService authService;
    private final ModelMapper modelMapper;
    private final MessageSource messageSource;

    @Autowired
    public PedidoController(IPedidoService pedidoService, ISucursalService sucursalService,
                            IUsuarioService usuarioService, IClienteService clienteService,
                            IAuthService authService, ModelMapper modelMapper, MessageSource messageSource) {
        this.pedidoService = pedidoService;
        this.sucursalService = sucursalService;
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
        this.authService = authService;
        this.modelMapper = modelMapper;
        this.messageSource = messageSource;
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
  public void actualizar(@RequestParam Long idSucursal,
                         @RequestParam Long idUsuario,
                         @RequestParam Long idCliente,
                         @RequestParam TipoDeEnvio tipoDeEnvio,
                         @RequestBody PedidoDTO pedidoDTO) {
    Pedido pedido = modelMapper.map(pedidoDTO, Pedido.class);
    pedido.setSucursal(sucursalService.getSucursalPorId(idSucursal));
    pedido.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuario));
    pedido.setCliente(clienteService.getClienteNoEliminadoPorId(idCliente));
    pedido.setDetalleEnvio(
        pedidoService.getPedidoNoEliminadoPorId(pedidoDTO.getIdPedido()).getDetalleEnvio());
    // Las facturas se recuperan para evitar cambios no deseados.
    pedido.setFacturas(pedidoService.getFacturasDelPedido(pedido.getIdPedido()));
    // Si los renglones vienen null, recupera los renglones del pedido para actualizar
    // caso contrario, ultiliza los renglones del pedido.
    if (pedido.getRenglones() == null) {
      pedido.setRenglones(
          pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglon(pedido.getIdPedido()));
    } else {
      List<RenglonPedido> renglonesActualizados = new ArrayList<>();
      pedido
          .getRenglones()
          .forEach(
              renglonPedido ->
                  renglonesActualizados.add(
                      pedidoService.calcularRenglonPedido(
                          renglonPedido.getIdProductoItem(),
                          renglonPedido.getCantidad(),
                          pedido.getCliente())));
      pedido.setRenglones(renglonesActualizados);
    }
    pedidoService.actualizar(pedido, tipoDeEnvio);
  }

  @PostMapping("/pedidos")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Pedido guardar(@RequestBody NuevoPedidoDTO nuevoPedidoDTO) {
    return pedidoService.guardar(nuevoPedidoDTO);
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
