package org.opencommercial.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.opencommercial.model.Transportista;
import org.opencommercial.service.TransportistaServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class TransportistaControllerTest {

  @Mock TransportistaServiceImpl transportistaService;

  @InjectMocks TransportistaController transportistaController;

  @Test
  void shouldGetTransportistaPorId() {
    Transportista transportista =new Transportista();
    transportista.setNombre("OCA");
    when(transportistaService.getTransportistaNoEliminadoPorId(1L)).thenReturn(transportista);
    assertEquals(transportista, transportistaController.getTransportistaPorId(1L));
  }

  @Test
  void shouldGetTransportistas() {
    Transportista transportista = new Transportista();
    transportista.setNombre("OCA");
    Transportista transportista2 = new Transportista();
    transportista2.setNombre("Raosa");
    List<Transportista> transportistas = new ArrayList<>();
    transportistas.add(transportista);
    transportistas.add(transportista2);
    when(transportistaService.getTransportistas()).thenReturn(transportistas);
    assertEquals(transportistas, transportistaController.getTransportistas());
  }
}
