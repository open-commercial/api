package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.EstadoPedido;
import sic.modelo.Pedido;
import sic.repository.PedidoRepository;
import sic.repository.RenglonPedidoRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PedidoServiceImpl.class)
class PedidoServiceImplTest {

  @MockBean PedidoRepository pedidoRepository;
  @MockBean RenglonPedidoRepository renglonPedidoRepository;
  @MockBean FacturaVentaServiceImpl facturaVentaService;
  @MockBean UsuarioServiceImpl usuarioService;
  @MockBean ClienteServiceImpl clienteService;
  @MockBean ProductoServiceImpl productoService;
  @MockBean CorreoElectronicoServiceImpl correoElectronicoService;
  @MockBean ConfiguracionSucursalServiceImpl configuracionSucursalService;
  @MockBean CuentaCorrienteServiceImpl cuentaCorrienteService;
  @MockBean ModelMapper modelMapper;

  @Autowired PedidoServiceImpl pedidoService;

  @Test
  void shouldEliminarPedido() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.ABIERTO);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    assertTrue(pedidoService.eliminar(1L));
    verify(pedidoRepository, times(1)).save(pedido);
  }
}
