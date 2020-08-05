package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.Remito;
import sic.repository.RemitoRepository;
import sic.repository.RenglonRemitoRepository;
import sic.service.*;
import sic.util.CustomValidator;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CustomValidator.class, RemitoServiceImpl.class})
class RemitoServiceImplTest {

  @MockBean IFacturaService facturaService;
  @MockBean IFacturaVentaService facturaVentaService;
  @MockBean RemitoRepository remitoRepository;
  @MockBean RenglonRemitoRepository renglonRemitoRepository;
  @MockBean IClienteService clienteService;
  @MockBean IUsuarioService usuarioService;
  @MockBean IConfiguracionSucursalService configuracionSucursalService;
  @MockBean ICuentaCorrienteService cuentaCorrienteService;
  @MockBean MessageSource messageSource;

  @Autowired RemitoServiceImpl remitoService;

  @Test
  void shouldGetRemitoPorId() {
    Remito remito = new Remito();
    when(remitoRepository.findById(1L)).thenReturn(Optional.of(remito));
    Remito remitoRecuperado = remitoService.getRemitoPorId(1L);
    assertNotNull(remitoRecuperado);
  }

  @Test
  void shouldCrearRemitoDeFacturaVenta() {

  }
}
