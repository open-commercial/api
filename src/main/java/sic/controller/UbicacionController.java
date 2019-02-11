package sic.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Rol;
import sic.modelo.Ubicacion;
import sic.modelo.dto.UbicacionDTO;
import sic.service.IClienteService;
import sic.service.IUbicacionService;

@RestController
@RequestMapping("/api/v1")
public class UbicacionController {

  private final IUbicacionService ubicacionService;
  private final IClienteService clienteService;
  private final ModelMapper modelMapper;

  @Autowired
  public UbicacionController(IUbicacionService ubicacionService,
                             IClienteService clienteService,
                             ModelMapper modelMapper) {
    this.ubicacionService = ubicacionService;
    this.clienteService = clienteService;
    this.modelMapper = modelMapper;
  }

  @PutMapping("/ubicaciones/envio/clientes/{idCliente}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
  public void actualizar(@RequestBody UbicacionDTO ubicacionDTO,
                         @PathVariable long idCliente) {
    Ubicacion ubicacion = modelMapper.map(ubicacionDTO, Ubicacion.class);
    ubicacionService.actualizarUbicacionEnvio(ubicacion, clienteService.getClientePorId(idCliente));
  }

}
