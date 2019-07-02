package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.TipoDeEnvio;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NuevaOrdenDeCarritoDeCompraDTO {

  private Long idEmpresa;
  private Long idUsuario;
  private Long idCliente;
  private TipoDeEnvio tipoDeEnvio;
  private Long idSucursal;
  private String observaciones;
}
