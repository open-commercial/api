package org.opencommercial.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opencommercial.model.Pedido;
import org.opencommercial.model.RenglonPedido;
import org.opencommercial.model.Resultados;
import org.opencommercial.model.dto.NuevoRenglonPedidoDTO;
import org.opencommercial.model.dto.NuevosResultadosComprobanteDTO;
import org.opencommercial.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PedidoController.class})
class PedidoControllerTest {

  @MockBean PedidoServiceImpl pedidoService;
  @MockBean UsuarioServiceImpl usuarioService;
  @MockBean SucursalServiceImpl sucursalService;
  @MockBean ClienteServiceImpl clienteService;
  @MockBean ReciboServiceImpl reciboService;
  @MockBean AuthServiceImpl authService;

  @Autowired PedidoController pedidoController;

  @Test
  void shouldGetPedidoPorId() {
    Pedido pedido = new Pedido();
    pedido.setNroPedido(1L);
    when(pedidoService.getPedidoNoEliminadoPorId(2L)).thenReturn(pedido);
    assertEquals(pedido, pedidoController.getPedidoPorId(2L));
  }

  @Test
  void shouldCalcularRenglonesPedido() {
    List<NuevoRenglonPedidoDTO> nuevoRenglonPedido = new ArrayList<>();
    nuevoRenglonPedido.add(NuevoRenglonPedidoDTO.builder().idProductoItem(1L).cantidad(BigDecimal.TEN).build());
    nuevoRenglonPedido.add(NuevoRenglonPedidoDTO.builder().idProductoItem(2L).cantidad(BigDecimal.ONE).build());
    List<RenglonPedido> renglones = new ArrayList<>();
    long[] idsProducto = {1L, 2L};
    BigDecimal[] cantidades = {BigDecimal.TEN, BigDecimal.ONE};
    when(pedidoService.calcularRenglonesPedido(idsProducto, cantidades)).thenReturn(renglones);
    assertEquals(renglones, pedidoController.calcularRenglonesPedido(nuevoRenglonPedido));
  }

  @Test
  void shouldCalcularResultadosPedido() {
    NuevosResultadosComprobanteDTO nuevosResultadosComprobanteDTO =
        NuevosResultadosComprobanteDTO.builder().build();
    Resultados resultados = new Resultados();
    when(pedidoService.calcularResultadosPedido(nuevosResultadosComprobanteDTO))
        .thenReturn(resultados);
    assertEquals(resultados, pedidoController.calcularResultadosPedido(nuevosResultadosComprobanteDTO));
  }
}
