package org.opencommercial.controller;

import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.opencommercial.model.Traspaso;
import org.opencommercial.model.criteria.BusquedaTraspasoCriteria;
import org.opencommercial.model.dto.NuevoTraspasoDTO;
import org.opencommercial.service.AuthServiceImpl;
import org.opencommercial.service.TraspasoServiceImpl;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TraspasoController.class})
class TraspasoControllerTest {

  @MockBean TraspasoServiceImpl traspasoService;
  @MockBean AuthServiceImpl authService;

  @Autowired TraspasoController traspasoController;

  @Test
  void shouldBuscarTraspasos() {
    BusquedaTraspasoCriteria traspasoCriteria = BusquedaTraspasoCriteria.builder().build();
    traspasoController.getTraspasosCriteria(traspasoCriteria);
    verify(traspasoService).buscarTraspasos(traspasoCriteria);
  }

  @Test
  void shouldGetRenglones() {
    traspasoController.getRenglonesDelTraspaso(1L);
    verify(traspasoService).getRenglonesTraspaso(1L);
  }

  @Test
  void shouldGuardarTraspaso() {
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    traspasoController.guardarTraspaso(NuevoTraspasoDTO.builder().build(), "headers");
    verify(traspasoService).guardarTraspaso(NuevoTraspasoDTO.builder().build(), 1L);
  }

  @Test
  void shouldEliminarTraspaso() {
    when(traspasoService.getTraspasoNoEliminadoPorid(1L)).thenReturn(new Traspaso());
    traspasoController.eliminarTraspaso(1L);
    verify(traspasoService).eliminar(1L);
  }
}
